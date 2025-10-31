package io.leavesfly.jtrade.agents.analysts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jtrade.agents.base.Agent;
import io.leavesfly.jtrade.agents.base.AgentType;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.dataflow.model.FundamentalData;
import io.leavesfly.jtrade.dataflow.model.NewsData;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
import io.leavesfly.jtrade.llm.client.LlmClient;
import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 支持工具使用的RecAgent（文本版ReAct循环）
 * 不改动 LlmClient 协议，通过提示词让LLM以“Action/Action Input/Observation/Final Answer”格式互动。
 */
@Slf4j
@Component
public class RecAgent implements Agent {

    private final LlmClient llmClient;
    private final DataAggregator dataAggregator;
    private final AppConfig appConfig;
    private final ModelConfig modelConfig;
    private final ObjectMapper mapper = new ObjectMapper();

    // 简单工具注册表
    private final Map<String, Tool> tools = new HashMap<>();

    public RecAgent(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        this.llmClient = llmClient;
        this.dataAggregator = dataAggregator;
        this.appConfig = appConfig;
        this.modelConfig = ModelConfig.builder()
                .temperature(0.3)
                .maxTokens(2000)
                .build();

        registerTools();
    }

    @Override
    public AgentState execute(AgentState state) {
        String symbol = state.getCompany();
        String dateStr = state.getDate() != null
                ? state.getDate().format(DateTimeFormatter.ISO_DATE)
                : "N/A";

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(buildSystemPrompt()));
        messages.add(LlmMessage.user(String.format(
                "目标：研究 %s 在 %s 的投资价值，并给出交易建议（BUY/SELL/HOLD），必要时调用工具。\n" +
                "请严格使用如下格式进行交互：\n" +
                "Thought: ...\n" +
                "Action: <tool_name>\n" +
                "Action Input: {json}\n" +
                "在获得我返回的观察（Observation）后，继续直到给出 Final Answer。\n" +
                "初始上下文：symbol=%s, date=%s",
                symbol, dateStr, symbol, dateStr
        )));

        List<String> trace = new ArrayList<>();
        int maxSteps = Optional.ofNullable(appConfig.getWorkflow())
                .map(AppConfig.WorkflowConfig::getMaxRecursionLimit).orElse(20);
        int step = 0;
        String finalAnswer = null;

        while (step < maxSteps) {
            step++;
            LlmResponse resp = llmClient.chat(messages, modelConfig);
            String content = resp.getContent();
            trace.add("ASSISTANT: " + content);

            // 如果已经给出最终答案，退出
            String fa = extractFinalAnswer(content);
            if (fa != null) {
                finalAnswer = fa;
                break;
            }

            // 解析Action与输入
            ActionCall call = extractActionCall(content);
            if (call == null) {
                // 没有动作，提示继续思考
                messages.add(LlmMessage.tool("Observation: 未检测到有效的Action，请给出下一步Action或最终答案。"));
                continue;
            }

            // 执行工具
            String observation;
            try {
                Tool tool = tools.get(call.name);
                if (tool == null) {
                    observation = "错误：未知工具 " + call.name + "。可用工具：" + String.join(", ", tools.keySet());
                } else {
                    observation = tool.run(call.input);
                }
            } catch (Exception e) {
                observation = "错误：工具执行失败 - " + e.getMessage();
            }

            trace.add("OBSERVATION: " + observation);
            messages.add(LlmMessage.tool("Observation: " + observation));
        }

        if (finalAnswer == null) {
            finalAnswer = "未在限制步数内得到最终答案，请谨慎决策。";
        }

        // 写入报告与元数据
        AgentState newState = state.addAnalystReport("【RecAgent 综合报告】\n" + finalAnswer)
                .putMetadata("recagent_trace", trace);

        log.info("RecAgent完成，最终答案长度：{}", finalAnswer.length());
        return newState;
    }

    @Override
    public String getName() {
        return "RecAgent";
    }

    @Override
    public AgentType getType() {
        return AgentType.REC_AGENT;
    }

    // ===== 工具注册与实现 =====

    private void registerTools() {
        tools.put("fundamentals", new Tool(
                "fundamentals",
                "获取公司基本面（市值、估值、分红、EPS等）。输入：{\"symbol\":\"TSLA\"}",
                input -> {
                    String symbol = (String) input.getOrDefault("symbol", "");
                    FundamentalData fd = dataAggregator.getFundamentalData(symbol);
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("symbol", fd.getSymbol());
                    out.put("companyName", fd.getCompanyName());
                    out.put("marketCap", String.valueOf(fd.getMarketCap()));
                    out.put("peRatio", String.valueOf(fd.getPeRatio()));
                    out.put("pbRatio", String.valueOf(fd.getPbRatio()));
                    out.put("dividendYield", String.valueOf(fd.getDividendYield()));
                    out.put("eps", String.valueOf(fd.getEps()));
                    return toJson(out);
                }
        ));

        tools.put("market_indicators", new Tool(
                "market_indicators",
                "获取技术指标（RSI、MACD、均线、布林带等）。输入：{\"symbol\":\"TSLA\"}",
                input -> {
                    String symbol = (String) input.getOrDefault("symbol", "");
                    Map<String, Double> ind = dataAggregator.getTechnicalIndicators(symbol);
                    return toJson(ind);
                }
        ));

        tools.put("news", new Tool(
                "news",
                "获取最近新闻（标题、来源、摘要、情绪评分）。输入：{\"symbol\":\"TSLA\",\"limit\":5}",
                input -> {
                    String symbol = (String) input.getOrDefault("symbol", "");
                    int limit = toInt(input.getOrDefault("limit", 5));
                    List<NewsData> news = dataAggregator.getNewsData(symbol, limit);
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (NewsData n : news) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("title", n.getTitle());
                        item.put("source", n.getSource());
                        item.put("url", n.getUrl());
                        item.put("publishedAt", String.valueOf(n.getPublishedAt()));
                        item.put("summary", n.getSummary());
                        item.put("sentimentScore", n.getSentimentScore());
                        list.add(item);
                    }
                    return toJson(list);
                }
        ));

        tools.put("social_sentiment", new Tool(
                "social_sentiment",
                "获取社媒情绪概览（总体、正负比例、帖子数）。输入：{\"symbol\":\"TSLA\"}",
                input -> {
                    String symbol = (String) input.getOrDefault("symbol", "");
                    Map<String, Object> sentiment = dataAggregator.getSocialMediaSentiment(symbol);
                    return toJson(sentiment);
                }
        ));
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个具备工具使用能力的研究型智能体（RecAgent）。\n");
        sb.append("可用工具列表：\n");
        for (Tool t : tools.values()) {
            sb.append("- ").append(t.name).append(": ").append(t.description).append("\n");
        }
        sb.append("\n交互规则（严格遵守）：\n");
        sb.append("1) 逐步推理：使用 Thought 描述思考；\n");
        sb.append("2) 当需要调用工具时，按如下格式输出：\n");
        sb.append("   Action: <tool_name>\n");
        sb.append("   Action Input: {json}\n");
        sb.append("3) 收到我返回的 Observation 后继续推理；\n");
        sb.append("4) 完成后以如下格式给出最终结论：\n");
        sb.append("   Final Answer: <你的结论与交易建议（BUY/SELL/HOLD）>\n");
        return sb.toString();
    }

    // ===== 解析与工具辅助 =====

    private static final Pattern ACTION_PATTERN =
            Pattern.compile("(?s)Action\\s*:\\s*(\\w+)\\s*\\n\\s*Action Input\\s*:\\s*(\\{.*?\\})");

    private ActionCall extractActionCall(String content) {
        Matcher m = ACTION_PATTERN.matcher(content);
        if (!m.find()) return null;
        String name = m.group(1).trim();
        String json = m.group(2).trim();
        try {
            JsonNode node = mapper.readTree(json);
            Map<String, Object> map = mapper.convertValue(node, Map.class);
            return new ActionCall(name, map);
        } catch (Exception e) {
            log.warn("Action Input解析失败: {}", e.getMessage());
            return new ActionCall(name, Collections.emptyMap());
        }
    }

    private String extractFinalAnswer(String content) {
        int idx = content.indexOf("Final Answer:");
        if (idx < 0) return null;
        return content.substring(idx + "Final Answer:".length()).trim();
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private int toInt(Object v) {
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return 5;
        }
    }

    // ===== 简易数据结构 =====

    private static class ActionCall {
        final String name;
        final Map<String, Object> input;
        ActionCall(String name, Map<String, Object> input) {
            this.name = name; this.input = input;
        }
    }

    private static class Tool {
        final String name;
        final String description;
        final ToolRunner runner;
        Tool(String name, String description, ToolRunner runner) {
            this.name = name; this.description = description; this.runner = runner;
        }
        String run(Map<String, Object> input) { 
            try {
                return runner.run(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FunctionalInterface
    private interface ToolRunner {
        String run(Map<String, Object> input) throws Exception;
    }
}
