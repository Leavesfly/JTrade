package io.leavesfly.jtrade.agents.trader;

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
 * 交易员
 * 
 * 基于研究经理的决策制定具体交易计划
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class Trader extends BaseRecAgent {

    public Trader(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        ReactResult result = performReact(state);
        AgentState updated = state.toBuilder()
                .tradingPlan(result.finalAnswer)
                .build();
        return updated.putMetadata("trader_trace", result.trace);
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位专业的交易员，负责将投资决策转化为具体的交易计划。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String symbol = state.getCompany();
        String managerDecision = state.getResearchManagerDecision();
        return String.format(
                "基于研究经理的决策，为 %s 制定可执行的交易计划（方向/时机/仓位/入场条件/止损止盈等），并在最终答案中给出完整计划文本。\n研究经理决策：\n%s",
                symbol, managerDecision
        );
    }
    
    @Override
    public String getName() {
        return "交易员";
    }
    
    @Override
    protected void registerAdditionalTools(Map<String, Tool> tools) {
        tools.put("position_sizing", new Tool(
                "position_sizing",
                "根据账户权益与风险参数计算仓位大小。输入：{\"account_equity\":100000,\"risk_percent\":0.01,\"stop_loss_distance\":2.5}",
                input -> {
                    double equity = toDouble(input.get("account_equity"), 100000.0);
                    double risk = toDouble(input.get("risk_percent"), 0.01);
                    double sld = toDouble(input.get("stop_loss_distance"), 1.0);
                    double riskAmount = equity * risk;
                    double sizeUnits = sld != 0 ? riskAmount / sld : 0.0;
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("account_equity", equity);
                    out.put("risk_percent", risk);
                    out.put("risk_amount", riskAmount);
                    out.put("stop_loss_distance", sld);
                    out.put("position_size_units", sizeUnits);
                    return toJson(out);
                }
        ));
    }
    
    private double toDouble(Object v, double def) {
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.trader.system 和 react.trader.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.trader";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.TRADER;
    }
}
