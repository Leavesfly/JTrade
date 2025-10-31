package io.leavesfly.jtrade.graph;

import io.leavesfly.jtrade.core.state.AgentState;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 图传播器 - 管理状态传播和日志
 * 
 * 对应 Python 版本的 Propagator
 * 负责状态初始化和历史记录
 * 
 * @author 山泽
 */
@Slf4j
public class GraphPropagator {
    
    // 状态历史记录 - 日期到状态的映射
    private final Map<LocalDate, AgentState> stateHistory = new HashMap<>();
    
    // 当前股票代码
    private String currentSymbol;
    
    /**
     * 创建初始状态
     */
    public AgentState createInitialState(String symbol, LocalDate date) {
        this.currentSymbol = symbol;
        
        log.info("创建初始状态: symbol={}, date={}", symbol, date);
        
        return AgentState.builder()
                .company(symbol)
                .date(date)
                .build();
    }
    
    /**
     * 记录状态到历史
     */
    public void logState(LocalDate date, AgentState state) {
        stateHistory.put(date, state);
        log.debug("记录状态: date={}, signal={}", date, state.getFinalSignal());
    }
    
    /**
     * 获取历史状态
     */
    public AgentState getHistoricalState(LocalDate date) {
        return stateHistory.get(date);
    }
    
    /**
     * 获取所有历史状态
     */
    public Map<LocalDate, AgentState> getAllStates() {
        return new HashMap<>(stateHistory);
    }
    
    /**
     * 清空历史
     */
    public void clearHistory() {
        stateHistory.clear();
        log.info("清空状态历史");
    }
    
    /**
     * 获取当前符号
     */
    public String getCurrentSymbol() {
        return currentSymbol;
    }
    
    /**
     * 获取状态数量
     */
    public int getStateCount() {
        return stateHistory.size();
    }
}
