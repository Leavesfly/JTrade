package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.service.TradingService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量测试演示
 * 
 * 测试多个股票的分析性能和准确性
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class BatchTestDemo {
    
    public static void main(String[] args) {
        SpringApplication.run(BatchTestDemo.class, args);
    }
    
    @Bean
    public CommandLineRunner batchTest(TradingService tradingService) {
        return args -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("JTrade 批量测试演示");
            System.out.println("=".repeat(80));
            System.out.println();
            
            // 测试股票列表
            String[] symbols = {"AAPL", "TSLA", "NVDA", "MSFT", "GOOGL"};
            LocalDate date = LocalDate.now();
            
            System.out.println("测试配置：");
            System.out.println("  测试股票数量: " + symbols.length);
            System.out.println("  分析日期: " + date);
            System.out.println();
            
            List<TestResult> results = new ArrayList<>();
            long totalStartTime = System.currentTimeMillis();
            
            // 执行批量测试
            for (int i = 0; i < symbols.length; i++) {
                String symbol = symbols[i];
                System.out.println("-".repeat(80));
                System.out.println("测试进度: [" + (i + 1) + "/" + symbols.length + "] " + symbol);
                System.out.println("-".repeat(80));
                
                long startTime = System.currentTimeMillis();
                TestResult result = new TestResult();
                result.setSymbol(symbol);
                result.setDate(date);
                
                try {
                    AgentState state = tradingService.executeTradingWorkflow(symbol, date);
                    long endTime = System.currentTimeMillis();
                    
                    result.setSuccess(true);
                    result.setExecutionTime(endTime - startTime);
                    result.setFinalSignal(state.getFinalSignal());
                    result.setAnalystReports(state.getAnalystReports().size());
                    result.setResearcherViewpoints(state.getResearcherViewpoints().size());
                    
                    System.out.println("✓ 测试成功");
                    System.out.println("  执行时间: " + result.getExecutionTime() + " ms");
                    System.out.println("  最终信号: " + result.getFinalSignal());
                    
                } catch (Exception e) {
                    long endTime = System.currentTimeMillis();
                    result.setSuccess(false);
                    result.setExecutionTime(endTime - startTime);
                    result.setErrorMessage(e.getMessage());
                    
                    System.out.println("✗ 测试失败");
                    System.out.println("  错误信息: " + e.getMessage());
                }
                
                results.add(result);
                System.out.println();
            }
            
            long totalEndTime = System.currentTimeMillis();
            long totalDuration = totalEndTime - totalStartTime;
            
            // 输出测试报告
            printTestReport(results, totalDuration);
        };
    }
    
    private void printTestReport(List<TestResult> results, long totalDuration) {
        System.out.println("=".repeat(80));
        System.out.println("批量测试报告");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // 统计信息
        int successCount = 0;
        int failureCount = 0;
        long totalExecutionTime = 0;
        int buyCount = 0;
        int sellCount = 0;
        int holdCount = 0;
        
        for (TestResult result : results) {
            if (result.isSuccess()) {
                successCount++;
                totalExecutionTime += result.getExecutionTime();
                
                if (result.getFinalSignal() != null) {
                    switch (result.getFinalSignal().toUpperCase()) {
                        case "BUY": buyCount++; break;
                        case "SELL": sellCount++; break;
                        case "HOLD": holdCount++; break;
                    }
                }
            } else {
                failureCount++;
            }
        }
        
        System.out.println("【整体统计】");
        System.out.println("  总测试数: " + results.size());
        System.out.println("  成功数: " + successCount + " (" + 
                String.format("%.1f%%", successCount * 100.0 / results.size()) + ")");
        System.out.println("  失败数: " + failureCount);
        System.out.println("  总耗时: " + totalDuration + " ms");
        System.out.println("  平均耗时: " + (successCount > 0 ? totalExecutionTime / successCount : 0) + " ms");
        System.out.println();
        
        System.out.println("【信号分布】");
        System.out.println("  BUY 信号: " + buyCount + " (" + 
                String.format("%.1f%%", buyCount * 100.0 / successCount) + ")");
        System.out.println("  SELL 信号: " + sellCount + " (" + 
                String.format("%.1f%%", sellCount * 100.0 / successCount) + ")");
        System.out.println("  HOLD 信号: " + holdCount + " (" + 
                String.format("%.1f%%", holdCount * 100.0 / successCount) + ")");
        System.out.println();
        
        // 详细结果表格
        System.out.println("【详细结果】");
        System.out.println("-".repeat(80));
        System.out.printf("%-10s %-15s %-10s %-15s %-10s%n", 
                "股票代码", "执行时间(ms)", "信号", "分析报告", "状态");
        System.out.println("-".repeat(80));
        
        for (TestResult result : results) {
            String status = result.isSuccess() ? "✓ 成功" : "✗ 失败";
            String signal = result.getFinalSignal() != null ? result.getFinalSignal() : "N/A";
            String reports = result.isSuccess() ? String.valueOf(result.getAnalystReports()) : "N/A";
            
            System.out.printf("%-10s %-15d %-10s %-15s %-10s%n",
                    result.getSymbol(),
                    result.getExecutionTime(),
                    signal,
                    reports,
                    status);
        }
        
        System.out.println("-".repeat(80));
        System.out.println();
        
        // 性能评估
        System.out.println("【性能评估】");
        if (successCount > 0) {
            long avgTime = totalExecutionTime / successCount;
            String performance;
            if (avgTime < 3000) {
                performance = "优秀";
            } else if (avgTime < 5000) {
                performance = "良好";
            } else if (avgTime < 10000) {
                performance = "一般";
            } else {
                performance = "需要优化";
            }
            System.out.println("  系统性能: " + performance);
            System.out.println("  平均响应时间: " + avgTime + " ms");
        }
        System.out.println();
        
        System.out.println("=".repeat(80));
        System.out.println("批量测试完成");
        System.out.println("=".repeat(80));
    }
    
    @Data
    static class TestResult {
        private String symbol;
        private LocalDate date;
        private boolean success;
        private long executionTime;
        private String finalSignal;
        private int analystReports;
        private int researcherViewpoints;
        private String errorMessage;
    }
}
