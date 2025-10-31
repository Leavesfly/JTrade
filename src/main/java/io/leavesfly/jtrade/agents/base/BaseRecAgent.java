package io.leavesfly.jtrade.agents.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.core.state.RiskDebateState;
import io.leavesfly.jtrade.dataflow.model.FundamentalData;
import io.leavesfly.jtrade.dataflow.model.NewsData;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
import io.leavesfly.jtrade.llm.client.LlmClient;
import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;
import io.leavesfly.jtrade.core.prompt.PromptManager;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抽象的具备工具能力的智能体基类（文本版 ReAct 循环）
 * 子类通过覆盖系统提示与初始用户提示，或附加工具注册来实现差异化。
 */
@Slf4j
public abstract class BaseRecAgent implements Agent {

    protected final LlmClient llmClient;
    protected final DataAggregator dataAggregator;
    protected final AppConfig appConfig;
    protected final ModelConfig modelConfig;
    protected final ObjectMapper mapper = new ObjectMapper();
    protected final PromptManager promptManager; // 可选，用于模板化管理

    // 工具注册表（子类可在构造中追加）
    protected final Map<String, Tool> tools = new LinkedHashMap<>();

    protected BaseRecAgent(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        this(llmClient, dataAggregator, appConfig, null);
    }

    protected BaseRecAgent(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig, PromptManager promptManager) {
        this.llmClient = llmClient;
        this.dataAggregator = dataAggregator;
        this.appConfig = appConfig;
        this.promptManager = promptManager;
        this.modelConfig = ModelConfig.builder()
                .temperature(0.3)
                .maxTokens(2000)
                .build();
        registerDefaultTools();
        registerAdditionalTools(this.tools);
    }

    /** 子类可覆盖以追加工具 */
    protected void registerAdditionalTools(Map<String, Tool> tools) {}

    /** 执行统一的 ReAct 循环 */
    @Override
    public AgentState execute(AgentState state) {
        ReactResult result = performReact(state);
        return state.addAnalystReport(String.format("【%s】\n%s", getName(), result.finalAnswer))
                .putMetadata(getName().toLowerCase() + "_trace", result.trace);
    }

    /** 实际执行ReAct循环，返回最终答案与轨迹 */
    protected ReactResult performReact(AgentState state) {
        String symbol = state.getCompany();
        String dateStr = state.getDate() != null
                ? state.getDate().format(DateTimeFormatter.ISO_DATE)
                : "N/A";

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(buildSystemPrompt()));
        messages.add(LlmMessage.user(buildInitialUserPrompt(state)));

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

            String fa = extractFinalAnswer(content);
            if (fa != null) {
                finalAnswer = fa;
                break;
            }

            ActionCall call = extractActionCall(content);
            if (call == null) {
                messages.add(LlmMessage.tool("Observation: 未检测到有效的Action，请给出下一步Action或最终答案。"));
                continue;
            }

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

        return new ReactResult(finalAnswer, trace);
    }

    /** ReAct执行结果容器 */
    protected static class ReactResult {
        public final String finalAnswer;
        public final List<String> trace;
        public ReactResult(String finalAnswer, List<String> trace) {
            this.finalAnswer = finalAnswer;
            this.trace = trace;
        }
    }

    /** 子类覆盖：系统提示（包含工具使用规范与角色职责） */
    protected String buildSystemPrompt() {
        // 尝试从 PromptManager 加载模板化提示
        if (promptManager != null) {
            String promptKey = getPromptKey();
            if (promptKey != null) {
                String systemPrompt = promptManager.getSystemPrompt(promptKey);
                if (systemPrompt != null && !systemPrompt.isEmpty()) {
                    // 替换工具列表占位符
                    String toolsList = buildToolsList();
                    return systemPrompt.replace("{tools}", toolsList);
                }
            }
        }
        
        // 降级到默认实现
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个具备工具使用能力的研究型智能体。\n");
        sb.append("可用工具列表：\n");
        sb.append(buildToolsList());
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

    /** 子类覆盖：首条用户意图（包含symbol/date与任务目标） */
    protected String buildInitialUserPrompt(AgentState state) {
        String symbol = state.getCompany();
        String dateStr = state.getDate() != null
                ? state.getDate().format(DateTimeFormatter.ISO_DATE)
                : "N/A";
        
        // 尝试从 PromptManager 加载模板化提示
        if (promptManager != null) {
            String promptKey = getPromptKey();
            if (promptKey != null) {
                String userPrompt = promptManager.getUserPrompt(promptKey);
                if (userPrompt != null && !userPrompt.isEmpty()) {
                    // 构建变量映射
                    Map<String, String> variables = buildPromptVariables(state);
                    return promptManager.buildPrompt(userPrompt, variables);
                }
            }
        }
        
        // 降级到默认实现
        return String.format(
                "目标：研究 %s 在 %s 的投资价值，并给出交易建议（BUY/SELL/HOLD），必要时调用工具。\n" +
                "请严格使用如下格式进行交互：\n" +
                "Thought: ...\n" +
                "Action: <tool_name>\n" +
                "Action Input: {json}\n" +
                "在获得我返回的观察（Observation）后，继续直到给出 Final Answer。\n" +
                "初始上下文：symbol=%s, date=%s",
                symbol, dateStr, symbol, dateStr
        );
    }
    
    /**
     * 子类覆盖：返回用于从 PromptManager 加载提示的键名
     * 例如："react.analyst.market" 用于市场分析师
     * 返回 null 将使用默认硬编码提示
     */
    protected String getPromptKey() {
        return null; // 默认不使用模板
    }
    
    /**
     * 子类可覆盖：构建提示变量映射
     */
    protected Map<String, String> buildPromptVariables(AgentState state) {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("symbol", state.getCompany());
        variables.put("date", state.getDate() != null 
                ? state.getDate().format(DateTimeFormatter.ISO_DATE) 
                : "N/A");
        
        // 添加各类上下文信息
        if (state.getAnalystReports() != null && !state.getAnalystReports().isEmpty()) {
            variables.put("analystReports", String.join("\n\n", state.getAnalystReports()));
        }
        if (state.getResearcherViewpoints() != null && !state.getResearcherViewpoints().isEmpty()) {
            variables.put("researcherViewpoints", String.join("\n\n", state.getResearcherViewpoints()));
        }
        if (state.getResearchManagerDecision() != null) {
            variables.put("managerDecision", state.getResearchManagerDecision());
        }
        if (state.getTradingPlan() != null) {
            variables.put("tradingPlan", state.getTradingPlan());
        }
        if (state.getRiskDebate() != null) {
            // 风险辩论状态转为字符串形式
            variables.put("riskDebateHistory", formatRiskDebate(state.getRiskDebate()));
        }
        
        return variables;
    }
    
    /**
     * 格式化风险辩论状态
     */
    private String formatRiskDebate(RiskDebateState debate) {
        if (debate == null) return "";
        StringBuilder sb = new StringBuilder();
        
        if (debate.getAggressiveStrategies() != null && !debate.getAggressiveStrategies().isEmpty()) {
            sb.append("【激进派观点】\n");
            for (String strategy : debate.getAggressiveStrategies()) {
                sb.append("- ").append(strategy).append("\n");
            }
            sb.append("\n");
        }
        
        if (debate.getConservativeStrategies() != null && !debate.getConservativeStrategies().isEmpty()) {
            sb.append("【保守派观点】\n");
            for (String strategy : debate.getConservativeStrategies()) {
                sb.append("- ").append(strategy).append("\n");
            }
            sb.append("\n");
        }
        
        if (debate.getNeutralStrategies() != null && !debate.getNeutralStrategies().isEmpty()) {
            sb.append("【中立派观点】\n");
            for (String strategy : debate.getNeutralStrategies()) {
                sb.append("- ").append(strategy).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 构建工具列表描述
     */
    private String buildToolsList() {
        StringBuilder sb = new StringBuilder();
        for (Tool t : tools.values()) {
            sb.append("- ").append(t.name).append(": ").append(t.description).append("\n");
        }
        return sb.toString();
    }

    // ===== 默认工具注册（可被子类扩展） =====
    protected void registerDefaultTools() {
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

    protected String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    protected int toInt(Object v) {
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return 5;
        }
    }

    // ===== 简易数据结构 =====
    protected static class ActionCall {
        final String name;
        final Map<String, Object> input;
        ActionCall(String name, Map<String, Object> input) {
            this.name = name; this.input = input;
        }
    }

    protected static class Tool {
        final String name;
        final String description;
        final ToolRunner runner;
        public Tool(String name, String description, ToolRunner runner) {
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
    protected interface ToolRunner {
        String run(Map<String, Object> input) throws Exception;
    }
}
