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
 * 保守风险辩论者
 * 
 * 倡导低风险稳健的投资策略，强调资产保护
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class ConservativeDebator extends BaseRecAgent {

    public ConservativeDebator(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        ReactResult result = performReact(state);
        String argument = "【保守观点】" + result.finalAnswer;
        RiskDebateState currentDebate = state.getRiskDebate();
        RiskDebateState newDebate = RiskDebateState.builder()
                .currentRound(currentDebate.getCurrentRound())
                .maxRounds(currentDebate.getMaxRounds())
                .aggressiveStrategies(currentDebate.getAggressiveStrategies())
                .conservativeStrategies(addToList(currentDebate.getConservativeStrategies(), argument))
                .neutralStrategies(currentDebate.getNeutralStrategies())
                .lastSpeaker("CONSERVATIVE")
                .build();
        AgentState updated = state.toBuilder().riskDebate(newDebate).build();
        return updated.putMetadata("conservative_trace", result.trace);
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位保守的风险分析师，擅长识别潜在风险与隐患，主张稳健策略。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String tradingPlan = state.getTradingPlan();
        RiskDebateState debate = state.getRiskDebate();
        return String.format(
                "基于交易计划与当前辩论内容，从保守角度提出风险识别与保护策略，直接回应对手观点，最终给出你的主张。\n交易计划：\n%s\n辩论：%s",
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
        return "保守风险辩论者";
    }
    
    @Override
    protected void registerAdditionalTools(Map<String, Tool> tools) {
        tools.put("risk_check", new Tool(
                "risk_check",
                "检查风险约束条件。输入：{\"position_size\":0.2,\"max_drawdown\":0.15,\"stop_loss\":0.05}",
                input -> {
                    double posSize = toDouble(input.get("position_size"), 0.1);
                    double maxDD = toDouble(input.get("max_drawdown"), 0.2);
                    double stopLoss = toDouble(input.get("stop_loss"), 0.05);
                    List<String> warnings = new ArrayList<>();
                    if (posSize > 0.3) warnings.add("仓位过大，建议不超过30%");
                    if (maxDD > 0.2) warnings.add("最大回撤超限，风险过高");
                    if (stopLoss < 0.03) warnings.add("止损位过窄，易被震出");
                    else if (stopLoss > 0.1) warnings.add("止损位过宽，单笔损失风险大");
                    boolean pass = warnings.isEmpty();
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("position_size", posSize);
                    out.put("max_drawdown", maxDD);
                    out.put("stop_loss", stopLoss);
                    out.put("check_pass", pass);
                    out.put("warnings", warnings);
                    return toJson(out);
                }
        ));
    }
    
    private double toDouble(Object v, double def) {
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.debator.conservative.system 和 react.debator.conservative.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.debator.conservative";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.RISK_MANAGER;
    }
}
