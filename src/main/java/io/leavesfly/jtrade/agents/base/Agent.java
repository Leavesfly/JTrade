package io.leavesfly.jtrade.agents.base;

import io.leavesfly.jtrade.core.state.AgentState;

/**
 * 智能体接口
 * 
 * 所有智能体必须实现此接口
 * 
 * @author 山泽
 */
public interface Agent {
    
    /**
     * 执行智能体逻辑
     * 
     * @param state 当前状态
     * @return 更新后的状态
     */
    AgentState execute(AgentState state);
    
    /**
     * 获取智能体名称
     * 
     * @return 智能体名称
     */
    String getName();
    
    /**
     * 获取智能体类型
     * 
     * @return 智能体类型
     */
    AgentType getType();
}
