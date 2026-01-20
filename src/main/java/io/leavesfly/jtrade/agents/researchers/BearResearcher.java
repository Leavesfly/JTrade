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
 * 空头研究员
 * 
 * 从看跌角度分析市场，提供卖出建议
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class BearResearcher extends BaseRecAgent {

    public BearResearcher(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        ReactResult result = performReact(state);
        return state.addResearcherViewpoint(String.format("【空头研究员】\n%s", result.finalAnswer))
                .putMetadata("bear_trace", result.trace);
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位看跌的研究员，擅长识别风险与做空机会。保持专业与客观。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String allReports = String.join("\n\n", state.getAnalystReports());
        String symbol = state.getCompany();
        return String.format(
                "请从看跌角度综合以下分析师报告，提出卖出/风险观点与论据，并最终给出建议（BUY/SELL/HOLD）。\n报告汇总：\n%s\n\n股票代码：%s",
                allReports, symbol
        );
    }
    
    @Override
    public String getName() {
        return "空头研究员";
    }
    
    @Override
    protected void registerAdditionalTools(Map<String, Tool> tools) {
        tools.put("bearish_signals", new Tool(
                "bearish_signals",
                "从给定指标中筛选看跌信号。输入：{\"rsi\":75,\"macd\":-1.5,\"volume_change\":-0.2}",
                input -> {
                    double rsi = toDouble(input.get("rsi"), 50.0);
                    double macd = toDouble(input.get("macd"), 0.0);
                    double volChg = toDouble(input.get("volume_change"), 0.0);
                    List<String> signals = new ArrayList<>();
                    if (rsi > 70) signals.add("RSI超买，回调压力大");
                    else if (rsi > 50) signals.add("RSI偏高，存在回调风险");
                    if (macd < 0) signals.add("MACD死叉，下跌动能增强");
                    if (volChg < -0.15) signals.add("成交量萎缩，卖盘压力显现");
                    if (signals.isEmpty()) signals.add("暂无明显看跌信号");
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("rsi", rsi);
                    out.put("macd", macd);
                    out.put("volume_change", volChg);
                    out.put("bearish_signals", signals);
                    return toJson(out);
                }
        ));
    }
    
    private double toDouble(Object v, double def) {
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.researcher.bear.system 和 react.researcher.bear.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.researcher.bear";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.BEAR_RESEARCHER;
    }
}
