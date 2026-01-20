package io.leavesfly.jtrade.agents.risk;

import io.leavesfly.jtrade.agents.base.BaseRecAgent;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.agents.base.AgentType;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.core.state.RiskDebateState;
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
 * 激进风险辩论者
 * 
 * 倡导高风险高回报的投资策略，挑战保守观点
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class AggressiveDebator extends BaseRecAgent {

    public AggressiveDebator(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        ReactResult result = performReact(state);
        String argument = "【激进观点】" + result.finalAnswer;
        RiskDebateState currentDebate = state.getRiskDebate();
        RiskDebateState newDebate = RiskDebateState.builder()
                .currentRound(currentDebate.getCurrentRound())
                .maxRounds(currentDebate.getMaxRounds())
                .aggressiveStrategies(addToList(currentDebate.getAggressiveStrategies(), argument))
                .conservativeStrategies(currentDebate.getConservativeStrategies())
                .neutralStrategies(currentDebate.getNeutralStrategies())
                .lastSpeaker("AGGRESSIVE")
                .build();
        AgentState updated = state.toBuilder().riskDebate(newDebate).build();
        return updated.putMetadata("aggressive_trace", result.trace);
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位激进的风险分析师，善于发现高收益机会，敢于挑战保守观点。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String tradingPlan = state.getTradingPlan();
        RiskDebateState debate = state.getRiskDebate();
        return String.format(
                "基于交易计划与当前辩论内容，从激进角度提出高收益策略与论据，直接回应对手观点，最终给出你的主张。\n交易计划：\n%s\n辩论：%s",
                tradingPlan, debate != null ? debate.toString() : "N/A"
        );
    }
    
    private List<String> addToList(List<String> list, String item) {
        List<String> newList = new ArrayList<>(list);
        newList.add(item);
        return newList;
    }
    
    @Override
    public String getName() {
        return "激进风险辩论者";
    }
    
    @Override
    protected void registerAdditionalTools(Map<String, Tool> tools) {
        tools.put("opportunity_score", new Tool(
                "opportunity_score",
                "评估高收益机会评分。输入：{\"growth_potential\":0.8,\"market_sentiment\":0.7,\"timing\":0.6}",
                input -> {
                    double growth = toDouble(input.get("growth_potential"), 0.5);
                    double sentiment = toDouble(input.get("market_sentiment"), 0.5);
                    double timing = toDouble(input.get("timing"), 0.5);
                    double score = (growth * 0.5 + sentiment * 0.3 + timing * 0.2);
                    String level = score > 0.7 ? "EXCELLENT" : (score > 0.5 ? "GOOD" : "MODERATE");
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("growth_potential", growth);
                    out.put("market_sentiment", sentiment);
                    out.put("timing", timing);
                    out.put("opportunity_score", score);
                    out.put("opportunity_level", level);
                    return toJson(out);
                }
        ));
    }
    
    private double toDouble(Object v, double def) {
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.debator.aggressive.system 和 react.debator.aggressive.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.debator.aggressive";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.RISK_MANAGER;
    }
}
