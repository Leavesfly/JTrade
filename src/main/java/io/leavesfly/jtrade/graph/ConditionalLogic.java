package io.leavesfly.jtrade.graph;

import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.core.state.RiskDebateState;

/**
 * 条件逻辑 - 处理工作流的条件判断
 * 
 * 对应 Python 版本的 ConditionalLogic
 * 决定工作流中的分支和循环
 * 
 * @author 山泽
 */
public class ConditionalLogic {
    
    /**
     * 判断辩论是否应该多头先发言
     */
    public boolean shouldContinueBullFirst(AgentState state) {
        // 如果还没有研究员观点，多头先发言
        if (state.getResearcherViewpoints().isEmpty()) {
            return true;
        }
        
        // 根据上一次发言者决定
        String lastViewpoint = state.getResearcherViewpoints().get(
            state.getResearcherViewpoints().size() - 1
        );
        
        // 如果上次是空头发言，这次多头发言
        return lastViewpoint.contains("空头") || lastViewpoint.contains("Bear");
    }
    
    /**
     * 判断辩论是否应该继续
     */
    public boolean shouldContinueDebate(AgentState state, int maxRounds) {
        int currentRounds = state.getResearcherViewpoints().size();
        return currentRounds < maxRounds * 2; // 两个研究员
    }
    
    /**
     * 判断风险辩论是否应该继续
     */
    public boolean shouldContinueRiskDebate(AgentState state, int maxRounds) {
        RiskDebateState riskDebate = state.getRiskDebate();
        if (riskDebate == null) {
            return true;
        }
        
        int totalStrategies = riskDebate.getAggressiveStrategies().size() +
                             riskDebate.getConservativeStrategies().size() +
                             riskDebate.getNeutralStrategies().size();
        
        return totalStrategies < maxRounds * 3; // 三个辩论者
    }
    
    /**
     * 判断下一个风险辩论发言者
     */
    public String getNextRiskSpeaker(AgentState state) {
        RiskDebateState riskDebate = state.getRiskDebate();
        
        if (riskDebate == null || riskDebate.getLastSpeaker() == null) {
            return "AGGRESSIVE";
        }
        
        String lastSpeaker = riskDebate.getLastSpeaker();
        
        // 按顺序轮转：激进 -> 保守 -> 中立 -> 激进
        switch (lastSpeaker) {
            case "AGGRESSIVE":
                return "CONSERVATIVE";
            case "CONSERVATIVE":
                return "NEUTRAL";
            case "NEUTRAL":
                return "AGGRESSIVE";
            default:
                return "AGGRESSIVE";
        }
    }
    
    /**
     * 判断是否需要额外的分析轮次
     */
    public boolean needsMoreAnalysis(AgentState state) {
        // 如果分析师报告少于3个，可能需要更多分析
        return state.getAnalystReports().size() < 3;
    }
    
    /**
     * 判断是否应该跳过风险辩论
     */
    public boolean shouldSkipRiskDebate(AgentState state) {
        // 如果交易计划为空或者是观望，可以跳过风险辩论
        String tradingPlan = state.getTradingPlan();
        return tradingPlan == null || 
               tradingPlan.isEmpty() || 
               tradingPlan.toUpperCase().contains("HOLD") ||
               tradingPlan.contains("观望");
    }
    
    /**
     * 判断是否应该进行深度反思
     */
    public boolean shouldDeepReflect(AgentState state) {
        // 如果决策信号是买入或卖出，进行深度反思
        String signal = state.getFinalSignal();
        return signal != null && 
               (signal.equals("BUY") || signal.equals("SELL"));
    }
}
