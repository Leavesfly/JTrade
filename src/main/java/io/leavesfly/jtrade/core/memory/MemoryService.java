package io.leavesfly.jtrade.core.memory;

import io.leavesfly.jtrade.core.state.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记忆服务
 * 
 * 存储和检索历史决策信息，支持学习和改进
 * 
 * @author 山泽
 */
@Slf4j
@Service
public class MemoryService {
    
    private final Map<String, List<DecisionMemory>> memoryStore = new ConcurrentHashMap<>();
    private final int maxMemoriesPerSymbol = 10;
    
    /**
     * 保存决策记忆
     */
    public void saveDecision(AgentState state) {
        if (state.getCompany() == null || state.getFinalSignal() == null) {
            log.warn("状态信息不完整，无法保存记忆");
            return;
        }
        
        String symbol = state.getCompany();
        DecisionMemory memory = new DecisionMemory();
        memory.setSymbol(symbol);
        memory.setDate(state.getDate());
        memory.setTimestamp(LocalDateTime.now());
        memory.setFinalSignal(state.getFinalSignal());
        memory.setAnalystReportCount(state.getAnalystReports().size());
        memory.setResearcherViewpointCount(state.getResearcherViewpoints().size());
        memory.setDecision(state.getResearchManagerDecision());
        memory.setTradingPlan(state.getTradingPlan());
        memory.setReflections(new ArrayList<>(state.getReflections()));
        
        // 存储记忆
        memoryStore.computeIfAbsent(symbol, k -> new ArrayList<>()).add(memory);
        
        // 限制记忆数量
        List<DecisionMemory> memories = memoryStore.get(symbol);
        if (memories.size() > maxMemoriesPerSymbol) {
            memories.remove(0); // 移除最旧的记忆
        }
        
        log.info("保存决策记忆: {} - {}", symbol, state.getFinalSignal());
    }
    
    /**
     * 获取历史决策
     */
    public List<DecisionMemory> getHistory(String symbol) {
        return memoryStore.getOrDefault(symbol, new ArrayList<>());
    }
    
    /**
     * 获取历史决策数量
     */
    public int getHistoryCount(String symbol) {
        return memoryStore.getOrDefault(symbol, new ArrayList<>()).size();
    }
    
    /**
     * 获取历史信号统计
     */
    public Map<String, Integer> getSignalStatistics(String symbol) {
        List<DecisionMemory> memories = getHistory(symbol);
        Map<String, Integer> stats = new HashMap<>();
        
        for (DecisionMemory memory : memories) {
            String signal = memory.getFinalSignal();
            stats.put(signal, stats.getOrDefault(signal, 0) + 1);
        }
        
        return stats;
    }
    
    /**
     * 获取最近的决策
     */
    public Optional<DecisionMemory> getLatestDecision(String symbol) {
        List<DecisionMemory> memories = getHistory(symbol);
        if (memories.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(memories.get(memories.size() - 1));
    }
    
    /**
     * 生成历史摘要
     */
    public String generateHistorySummary(String symbol) {
        List<DecisionMemory> memories = getHistory(symbol);
        if (memories.isEmpty()) {
            return "无历史决策记录";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("【%s 历史决策摘要】\n", symbol));
        summary.append(String.format("总决策次数: %d\n", memories.size()));
        
        Map<String, Integer> stats = getSignalStatistics(symbol);
        summary.append("信号分布:\n");
        stats.forEach((signal, count) -> 
            summary.append(String.format("  %s: %d 次 (%.1f%%)\n", 
                signal, count, count * 100.0 / memories.size()))
        );
        
        Optional<DecisionMemory> latest = getLatestDecision(symbol);
        latest.ifPresent(memory -> {
            summary.append(String.format("\n最近决策:\n"));
            summary.append(String.format("  日期: %s\n", memory.getDate()));
            summary.append(String.format("  信号: %s\n", memory.getFinalSignal()));
            summary.append(String.format("  时间: %s\n", memory.getTimestamp()));
        });
        
        return summary.toString();
    }
    
    /**
     * 清空特定股票的记忆
     */
    public void clearHistory(String symbol) {
        memoryStore.remove(symbol);
        log.info("清空 {} 的历史记忆", symbol);
    }
    
    /**
     * 清空所有记忆
     */
    public void clearAllHistory() {
        memoryStore.clear();
        log.info("清空所有历史记忆");
    }
    
    /**
     * 获取所有已记录的股票符号
     */
    public Set<String> getAllSymbols() {
        return memoryStore.keySet();
    }
    
    /**
     * 决策记忆
     */
    @Data
    public static class DecisionMemory {
        private String symbol;
        private java.time.LocalDate date;
        private LocalDateTime timestamp;
        private String finalSignal;
        private int analystReportCount;
        private int researcherViewpointCount;
        private String decision;
        private String tradingPlan;
        private List<String> reflections;
    }
}
