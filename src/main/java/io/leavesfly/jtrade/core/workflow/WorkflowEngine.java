package io.leavesfly.jtrade.core.workflow;

import io.leavesfly.jtrade.agents.base.Agent;
import io.leavesfly.jtrade.core.state.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 工作流引擎
 * 
 * 提供灵活的工作流编排能力，支持顺序执行、条件分支、循环等
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class WorkflowEngine {
    
    private final List<WorkflowNode> nodes = new ArrayList<>();
    
    /**
     * 添加顺序执行节点
     */
    public WorkflowEngine addNode(String name, Agent agent) {
        nodes.add(new WorkflowNode(name, agent, null, null));
        return this;
    }
    
    /**
     * 添加条件执行节点
     */
    public WorkflowEngine addConditionalNode(String name, Agent agent, Predicate<AgentState> condition) {
        nodes.add(new WorkflowNode(name, agent, condition, null));
        return this;
    }
    
    /**
     * 添加转换节点（不执行Agent，只转换状态）
     */
    public WorkflowEngine addTransformNode(String name, Function<AgentState, AgentState> transformer) {
        nodes.add(new WorkflowNode(name, null, null, transformer));
        return this;
    }
    
    /**
     * 执行工作流
     */
    public AgentState execute(AgentState initialState) {
        log.info("开始执行工作流，初始状态: {}", initialState.getCompany());
        
        AgentState currentState = initialState;
        int nodeIndex = 0;
        
        for (WorkflowNode node : nodes) {
            nodeIndex++;
            log.info("执行节点 [{}/{}]: {}", nodeIndex, nodes.size(), node.getName());
            
            try {
                // 检查条件
                if (node.getCondition() != null && !node.getCondition().test(currentState)) {
                    log.info("节点 {} 条件不满足，跳过执行", node.getName());
                    continue;
                }
                
                // 执行节点
                if (node.getAgent() != null) {
                    currentState = node.getAgent().execute(currentState);
                } else if (node.getTransformer() != null) {
                    currentState = node.getTransformer().apply(currentState);
                }
                
                log.info("节点 {} 执行成功", node.getName());
                
            } catch (Exception e) {
                log.error("节点 {} 执行失败", node.getName(), e);
                // 继续执行下一个节点，或者根据策略决定是否中断
            }
        }
        
        log.info("工作流执行完成");
        return currentState;
    }
    
    /**
     * 清空工作流
     */
    public void clear() {
        nodes.clear();
    }
    
    /**
     * 获取节点数量
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * 工作流节点
     */
    private static class WorkflowNode {
        private final String name;
        private final Agent agent;
        private final Predicate<AgentState> condition;
        private final Function<AgentState, AgentState> transformer;
        
        public WorkflowNode(String name, Agent agent, 
                          Predicate<AgentState> condition,
                          Function<AgentState, AgentState> transformer) {
            this.name = name;
            this.agent = agent;
            this.condition = condition;
            this.transformer = transformer;
        }
        
        public String getName() {
            return name;
        }
        
        public Agent getAgent() {
            return agent;
        }
        
        public Predicate<AgentState> getCondition() {
            return condition;
        }
        
        public Function<AgentState, AgentState> getTransformer() {
            return transformer;
        }
    }
}
