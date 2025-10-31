package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对比分析演示
 * 
 * 对比分析多个股票，提供横向对比和排名
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class ComparativeAnalysisDemo {
    
    public static void main(String[] args) {
        SpringApplication.run(ComparativeAnalysisDemo.class, args);
    }
    
    @Bean
    public CommandLineRunner comparativeDemo(TradingService tradingService) {
        return args -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("JTrade 对比分析演示");
            System.out.println("=".repeat(80));
            System.out.println();
            
            // 科技股对比
            String[] techStocks = {"AAPL", "MSFT", "GOOGL", "NVDA", "TSLA"};
            LocalDate date = LocalDate.now();
            
            System.out.println("对比组合：科技股票");
            System.out.println("  股票列表: " + String.join(", ", techStocks));
            System.out.println("  分析日期: " + date);
            System.out.println();
            System.out.println("正在执行分析...");
            System.out.println();
            
            Map<String, AgentState> results = new HashMap<>();
            
            // 分析每个股票
            for (String symbol : techStocks) {
                System.out.print("  ▸ 分析 " + symbol + "...");
                try {
                    AgentState state = tradingService.executeTradingWorkflow(symbol, date);
                    results.put(symbol, state);
                    System.out.println(" ✓");
                } catch (Exception e) {
                    System.out.println(" ✗ (失败: " + e.getMessage() + ")");
                }
            }
            
            System.out.println();
            
            // 生成对比报告
            printComparativeReport(results);
        };
    }
    
    private void printComparativeReport(Map<String, AgentState> results) {
        System.out.println("=".repeat(80));
        System.out.println("对比分析报告");
        System.out.println("=".repeat(80));
        System.out.println();
        
        if (results.isEmpty()) {
            System.out.println("没有成功的分析结果");
            return;
        }
        
        // 1. 交易信号对比
        System.out.println("【交易信号对比】");
        System.out.println("-".repeat(80));
        System.out.printf("%-15s %-15s %-50s%n", "股票代码", "最终信号", "信号强度");
        System.out.println("-".repeat(80));
        
        List<String> buyStocks = new ArrayList<>();
        List<String> sellStocks = new ArrayList<>();
        List<String> holdStocks = new ArrayList<>();
        
        for (Map.Entry<String, AgentState> entry : results.entrySet()) {
            String symbol = entry.getKey();
            AgentState state = entry.getValue();
            String signal = state.getFinalSignal() != null ? state.getFinalSignal() : "UNKNOWN";
            String signalBar = getSignalBar(signal);
            
            System.out.printf("%-15s %-15s %-50s%n", symbol, signal, signalBar);
            
            switch (signal.toUpperCase()) {
                case "BUY": buyStocks.add(symbol); break;
                case "SELL": sellStocks.add(symbol); break;
                case "HOLD": holdStocks.add(symbol); break;
            }
        }
        System.out.println("-".repeat(80));
        System.out.println();
        
        // 2. 信号分组统计
        System.out.println("【信号分组】");
        System.out.println("  📈 买入建议 (" + buyStocks.size() + "): " + 
                (buyStocks.isEmpty() ? "无" : String.join(", ", buyStocks)));
        System.out.println("  📉 卖出建议 (" + sellStocks.size() + "): " + 
                (sellStocks.isEmpty() ? "无" : String.join(", ", sellStocks)));
        System.out.println("  ⏸️  持有建议 (" + holdStocks.size() + "): " + 
                (holdStocks.isEmpty() ? "无" : String.join(", ", holdStocks)));
        System.out.println();
        
        // 3. 分析深度对比
        System.out.println("【分析深度对比】");
        System.out.println("-".repeat(80));
        System.out.printf("%-15s %-15s %-15s %-15s%n", 
                "股票代码", "分析师报告", "研究员观点", "完整度");
        System.out.println("-".repeat(80));
        
        for (Map.Entry<String, AgentState> entry : results.entrySet()) {
            String symbol = entry.getKey();
            AgentState state = entry.getValue();
            int reports = state.getAnalystReports().size();
            int viewpoints = state.getResearcherViewpoints().size();
            
            int completeness = 0;
            if (reports > 0) completeness += 40;
            if (viewpoints > 0) completeness += 30;
            if (state.getResearchManagerDecision() != null) completeness += 15;
            if (state.getTradingPlan() != null) completeness += 15;
            
            System.out.printf("%-15s %-15d %-15d %-15s%n",
                    symbol, reports, viewpoints, completeness + "%");
        }
        System.out.println("-".repeat(80));
        System.out.println();
        
        // 4. 投资建议排名
        System.out.println("【投资建议排名】");
        if (!buyStocks.isEmpty()) {
            System.out.println("  推荐买入顺序（基于分析深度）：");
            int rank = 1;
            for (String symbol : buyStocks) {
                AgentState state = results.get(symbol);
                int score = state.getAnalystReports().size() * 10 + 
                           state.getResearcherViewpoints().size() * 5;
                System.out.println("    " + rank++ + ". " + symbol + " (分析分数: " + score + ")");
            }
        } else {
            System.out.println("  暂无买入建议");
        }
        System.out.println();
        
        // 5. 风险提示
        System.out.println("【风险提示】");
        if (!sellStocks.isEmpty()) {
            System.out.println("  ⚠️  以下股票建议规避或减持：");
            for (String symbol : sellStocks) {
                System.out.println("    • " + symbol);
            }
        } else {
            System.out.println("  ✓ 分析组合中暂无明显风险信号");
        }
        System.out.println();
        
        // 6. 投资组合建议
        System.out.println("【投资组合建议】");
        if (!buyStocks.isEmpty()) {
            System.out.println("  建议配置：");
            double allocation = 100.0 / buyStocks.size();
            for (String symbol : buyStocks) {
                System.out.println("    • " + symbol + ": " + 
                        String.format("%.1f%%", allocation) + " 仓位");
            }
        } else {
            System.out.println("  建议保持现金观望");
        }
        System.out.println();
        
        System.out.println("=".repeat(80));
        System.out.println("对比分析完成");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("免责声明：本分析仅供参考，不构成投资建议。投资有风险，决策需谨慎。");
        System.out.println();
    }
    
    private String getSignalBar(String signal) {
        switch (signal.toUpperCase()) {
            case "BUY":
                return "████████████████████ (强烈买入)";
            case "SELL":
                return "▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ (强烈卖出)";
            case "HOLD":
                return "░░░░░░░░░░░░░░░░░░░░ (保持观望)";
            default:
                return "???????????????????? (未知)";
        }
    }
}
