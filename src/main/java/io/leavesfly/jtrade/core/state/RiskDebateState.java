package io.leavesfly.jtrade.core.state;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 风险辩论状态
 * 
 * 记录激进、保守、中立分析师之间的风险辩论历史
 * 
 * @author 山泽
 */
@Data
@Builder(toBuilder = true)
public class RiskDebateState {
    
    /**
     * 当前辩论轮次
     */
    @Builder.Default
    private int currentRound = 0;
    
    /**
     * 最大辩论轮数
     */
    @Builder.Default
    private int maxRounds = 1;
    
    /**
     * 激进策略列表
     */
    @Builder.Default
    private List<String> aggressiveStrategies = new ArrayList<>();
    
    /**
     * 保守策略列表
     */
    @Builder.Default
    private List<String> conservativeStrategies = new ArrayList<>();
    
    /**
     * 中立策略列表
     */
    @Builder.Default
    private List<String> neutralStrategies = new ArrayList<>();
    
    /**
     * 最后发言者
     */
    private String lastSpeaker;
    
    /**
     * 辩论是否结束
     */
    public boolean isDebateFinished() {
        return currentRound >= maxRounds;
    }
    
    /**
     * 添加激进策略
     */
    public RiskDebateState addAggressiveStrategy(String strategy) {
        List<String> newStrategies = new ArrayList<>(this.aggressiveStrategies);
        newStrategies.add(strategy);
        return this.toBuilder().aggressiveStrategies(newStrategies).build();
    }
    
    /**
     * 添加保守策略
     */
    public RiskDebateState addConservativeStrategy(String strategy) {
        List<String> newStrategies = new ArrayList<>(this.conservativeStrategies);
        newStrategies.add(strategy);
        return this.toBuilder().conservativeStrategies(newStrategies).build();
    }
    
    /**
     * 添加中立策略
     */
    public RiskDebateState addNeutralStrategy(String strategy) {
        List<String> newStrategies = new ArrayList<>(this.neutralStrategies);
        newStrategies.add(strategy);
        return this.toBuilder().neutralStrategies(newStrategies).build();
    }
    
    /**
     * 进入下一轮
     */
    public RiskDebateState nextRound() {
        return this.toBuilder().currentRound(this.currentRound + 1).build();
    }
}
