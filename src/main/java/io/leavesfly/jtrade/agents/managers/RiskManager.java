package io.leavesfly.jtrade.agents.managers;

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

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * 风险管理器
 * 
 * 评估交易计划的风险，做出最终批准决策
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class RiskManager extends BaseRecAgent {
    
    // Deleted: moved to BaseRecAgent
    // Deleted: moved to BaseRecAgent
    
    public RiskManager(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        ReactResult result = performReact(state);
        String finalSignal = extractSignal(result.finalAnswer);
        AgentState updated = state.toBuilder()
                .riskManagerDecision(result.finalAnswer)
                .finalSignal(finalSignal)
                .build();
        return updated.putMetadata("risk_manager_trace", result.trace);
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位严谨的风险管理器，负责评估交易计划的风险并做出最终批准决策。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String symbol = state.getCompany();
        RiskDebateState riskDebate = state.getRiskDebate();
        String tradingPlan = state.getTradingPlan();
        return String.format(
                "请基于交易计划与风险辩论内容进行风险评估，做出最终决策（APPROVE/REJECT/MODIFY），并明确最终交易信号（BUY/SELL/HOLD）。\n股票代码：%s\n交易计划：\n%s\n风险辩论：%s",
                symbol, tradingPlan, riskDebate != null ? riskDebate.toString() : "N/A"
        );
    }
    
    /**
     * 从决策文本中提取交易信号
     */
    private String extractSignal(String decision) {
        String upperDecision = decision.toUpperCase();
        
        if (upperDecision.contains("BUY") || upperDecision.contains("买入")) {
            return "BUY";
        } else if (upperDecision.contains("SELL") || upperDecision.contains("卖出")) {
            return "SELL";
        } else {
            return "HOLD";
        }
    }
    
    @Override
    public String getName() {
        return "风险管理器";
    }
    
    @Override
    protected void registerAdditionalTools(Map<String, Tool> tools) {
        tools.put("risk_score", new Tool(
                "risk_score",
                "根据输入风险因子计算风险评分。输入：{\"volatility\":0.3,\"liquidity\":0.8,\"leverage\":2}",
                input -> {
                    double vol = toDouble(input.get("volatility"), 0.3);
                    double liq = toDouble(input.get("liquidity"), 0.8);
                    double lev = toDouble(input.get("leverage"), 1.0);
                    double score = clamp(vol * 0.5 + (1 - liq) * 0.3 + (lev / 10.0) * 0.2, 0.0, 1.0);
                    String level = score > 0.7 ? "HIGH" : (score > 0.4 ? "MEDIUM" : "LOW");
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("volatility", vol);
                    out.put("liquidity", liq);
                    out.put("leverage", lev);
                    out.put("risk_score", score);
                    out.put("risk_level", level);
                    return toJson(out);
                }
        ));
    }
    
    private double toDouble(Object v, double def) {
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
    }
    
    private double clamp(double x, double lo, double hi) { return Math.max(lo, Math.min(hi, x)); }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.manager.risk.system 和 react.manager.risk.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.manager.risk";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.RISK_MANAGER;
    }
}
