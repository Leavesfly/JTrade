package io.leavesfly.jtrade.agents.researchers;

import io.leavesfly.jtrade.agents.base.BaseRecAgent;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.agents.base.AgentType;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.llm.client.LlmClient;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * 多头研究员
 * 
 * 从看涨角度分析市场，提供买入建议
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class BullResearcher extends BaseRecAgent {
    
    // Deleted: moved to BaseRecAgent
    // Deleted: moved to BaseRecAgent
    
    public BullResearcher(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        ReactResult result = performReact(state);
        return state.addResearcherViewpoint(String.format("【多头研究员】\n%s", result.finalAnswer))
                .putMetadata("bull_trace", result.trace);
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位看涨的研究员，专注于寻找买入机会。保持专业与客观。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String allReports = String.join("\n\n", state.getAnalystReports());
        String symbol = state.getCompany();
        return String.format(
                "请从看涨角度综合以下分析师报告，提出买入观点与论据，并最终给出建议（BUY/SELL/HOLD）。\n报告汇总：\n%s\n\n股票代码：%s",
                allReports, symbol
        );
    }
    
    @Override
    public String getName() {
        return "多头研究员";
    }
    
    @Override
    protected void registerAdditionalTools(Map<String, Tool> tools) {
        tools.put("bullish_signals", new Tool(
                "bullish_signals",
                "从给定指标中筛选看涨信号。输入：{\"rsi\":45,\"macd\":1.2,\"volume_change\":0.3}",
                input -> {
                    double rsi = toDouble(input.get("rsi"), 50.0);
                    double macd = toDouble(input.get("macd"), 0.0);
                    double volChg = toDouble(input.get("volume_change"), 0.0);
                    List<String> signals = new ArrayList<>();
                    if (rsi < 30) signals.add("RSI超卖，看涨反弹机会");
                    else if (rsi < 50) signals.add("RSI偏低，有上行空间");
                    if (macd > 0) signals.add("MACD金叉，上涨动能增强");
                    if (volChg > 0.2) signals.add("成交量放大，买入意愿强");
                    if (signals.isEmpty()) signals.add("暂无明显看涨信号");
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("rsi", rsi);
                    out.put("macd", macd);
                    out.put("volume_change", volChg);
                    out.put("bullish_signals", signals);
                    return toJson(out);
                }
        ));
    }
    
    private double toDouble(Object v, double def) {
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.researcher.bull.system 和 react.researcher.bull.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.researcher.bull";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.BULL_RESEARCHER;
    }
}
