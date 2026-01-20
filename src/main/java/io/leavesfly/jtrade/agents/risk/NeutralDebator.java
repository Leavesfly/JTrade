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
 * 中立风险辩论者
 * 
 * 平衡风险与收益，提供折中的投资策略建议
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class NeutralDebator extends BaseRecAgent {

    public NeutralDebator(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        ReactResult result = performReact(state);
        String argument = "【中立观点】" + result.finalAnswer;
        RiskDebateState currentDebate = state.getRiskDebate();
        RiskDebateState newDebate = RiskDebateState.builder()
                .currentRound(currentDebate.getCurrentRound())
                .maxRounds(currentDebate.getMaxRounds())
                .aggressiveStrategies(currentDebate.getAggressiveStrategies())
                .conservativeStrategies(currentDebate.getConservativeStrategies())
                .neutralStrategies(addToList(currentDebate.getNeutralStrategies(), argument))
                .lastSpeaker("NEUTRAL")
                .build();
        AgentState updated = state.toBuilder().riskDebate(newDebate).build();
        return updated.putMetadata("neutral_trace", result.trace);
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位中立的风险分析师，善于平衡风险与收益，寻找最优的风险收益比。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String tradingPlan = state.getTradingPlan();
        RiskDebateState debate = state.getRiskDebate();
        return String.format(
                "基于交易计划与当前辩论内容，从中立角度综合双方观点，提出折中策略，并最终给出你的主张。\n交易计划：\n%s\n辩论：%s",
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
        return "中立风险辩论者";
    }
    
    @Override
    protected void registerAdditionalTools(Map<String, Tool> tools) {
        tools.put("risk_reward_ratio", new Tool(
                "risk_reward_ratio",
                "计算风险收益比。输入：{\"entry_price\":100,\"stop_loss\":95,\"target_price\":110}",
                input -> {
                    double entry = toDouble(input.get("entry_price"), 100.0);
                    double stop = toDouble(input.get("stop_loss"), 95.0);
                    double target = toDouble(input.get("target_price"), 110.0);
                    double risk = Math.abs(entry - stop);
                    double reward = Math.abs(target - entry);
                    double ratio = risk > 0 ? reward / risk : 0.0;
                    String assessment = ratio >= 3 ? "优秀" : (ratio >= 2 ? "良好" : (ratio >= 1 ? "一般" : "不佳"));
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("entry_price", entry);
                    out.put("stop_loss", stop);
                    out.put("target_price", target);
                    out.put("risk", risk);
                    out.put("reward", reward);
                    out.put("risk_reward_ratio", ratio);
                    out.put("assessment", assessment);
                    return toJson(out);
                }
        ));
    }
    
    private double toDouble(Object v, double def) {
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.debator.neutral.system 和 react.debator.neutral.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.debator.neutral";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.RISK_MANAGER;
    }
}
