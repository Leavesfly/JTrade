package io.leavesfly.jtrade.core.state;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 投资辩论状态
 * 
 * 记录多头和空头研究员之间的辩论历史
 * 
 * @author 山泽
 */
@Data
@Builder(toBuilder = true)
public class InvestDebateState {
    
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
     * 多头观点列表
     */
    @Builder.Default
    private List<String> bullArguments = new ArrayList<>();
    
    /**
     * 空头观点列表
     */
    @Builder.Default
    private List<String> bearArguments = new ArrayList<>();
    
    /**
     * 辩论是否结束
     */
    public boolean isDebateFinished() {
        return currentRound >= maxRounds;
    }
    
    /**
     * 添加多头观点
     */
    public InvestDebateState addBullArgument(String argument) {
        List<String> newArguments = new ArrayList<>(this.bullArguments);
        newArguments.add(argument);
        return this.toBuilder().bullArguments(newArguments).build();
    }
    
    /**
     * 添加空头观点
     */
    public InvestDebateState addBearArgument(String argument) {
        List<String> newArguments = new ArrayList<>(this.bearArguments);
        newArguments.add(argument);
        return this.toBuilder().bearArguments(newArguments).build();
    }
    
    /**
     * 进入下一轮
     */
    public InvestDebateState nextRound() {
        return this.toBuilder().currentRound(this.currentRound + 1).build();
    }
}
