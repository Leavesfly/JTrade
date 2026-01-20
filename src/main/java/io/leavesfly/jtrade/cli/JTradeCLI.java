package io.leavesfly.jtrade.cli;

import io.leavesfly.jtrade.core.memory.MemoryService;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * JTrade 命令行界面
 * 
 * 提供完整的CLI交互功能
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class JTradeCLI {
    
    private static final String VERSION = "1.0.0";
    
    public static void main(String[] args) {
        SpringApplication.run(JTradeCLI.class, args);
    }
    
    @Bean
    public CommandLineRunner jTradeCliRunner(TradingService tradingService, MemoryService memoryService) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            
            printBanner();
            printHelp();
            
            while (true) {
                System.out.print("\njtrade> ");
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                String[] parts = input.split("\\s+");
                String command = parts[0].toLowerCase();
                
                try {
                    switch (command) {
                        case "analyze":
                        case "a":
                            handleAnalyze(parts, tradingService);
                            break;
                            
                        case "history":
                        case "h":
                            handleHistory(parts, memoryService);
                            break;
                            
                        case "list":
                        case "l":
                            handleList(memoryService);
                            break;
                            
                        case "clear":
                            handleClear(parts, memoryService);
                            break;
                            
                        case "help":
                        case "?":
                            printHelp();
                            break;
                            
                        case "version":
                        case "v":
                            printVersion();
                            break;
                            
                        case "exit":
                        case "quit":
                        case "q":
                            System.out.println("\n感谢使用 JTrade！再见！");
                            scanner.close();
                            return;
                            
                        default:
                            System.out.println("未知命令: " + command);
                            System.out.println("输入 'help' 查看帮助");
                    }
                } catch (Exception e) {
                    System.err.println("命令执行失败: " + e.getMessage());
                    log.error("命令执行错误", e);
                }
            }
        };
    }
    
    private void handleAnalyze(String[] parts, TradingService tradingService) {
        if (parts.length < 2) {
            System.out.println("用法: analyze <股票代码> [日期]");
            System.out.println("示例: analyze AAPL");
            System.out.println("示例: analyze TSLA 2024-01-15");
            return;
        }
        
        String symbol = parts[1].toUpperCase();
        LocalDate date = LocalDate.now();
        
        if (parts.length >= 3) {
            try {
                date = LocalDate.parse(parts[2], DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                System.out.println("日期格式错误，使用今天: " + date);
            }
        }
        
        System.out.println("\n开始分析 " + symbol + " (" + date + ")...");
        System.out.println("-".repeat(60));
        
        long startTime = System.currentTimeMillis();
        AgentState result = tradingService.executeTradingWorkflow(symbol, date);
        long duration = System.currentTimeMillis() - startTime;
        
        printAnalysisResult(result, duration);
    }
    
    private void handleHistory(String[] parts, MemoryService memoryService) {
        if (parts.length < 2) {
            System.out.println("用法: history <股票代码>");
            System.out.println("示例: history AAPL");
            return;
        }
        
        String symbol = parts[1].toUpperCase();
        String summary = memoryService.generateHistorySummary(symbol);
        System.out.println("\n" + summary);
    }
    
    private void handleList(MemoryService memoryService) {
        var symbols = memoryService.getAllSymbols();
        
        if (symbols.isEmpty()) {
            System.out.println("\n暂无历史记录");
            return;
        }
        
        System.out.println("\n已分析的股票列表:");
        System.out.println("-".repeat(60));
        
        for (String symbol : symbols) {
            int count = memoryService.getHistoryCount(symbol);
            var latest = memoryService.getLatestDecision(symbol);
            
            if (latest.isPresent()) {
                System.out.printf("%-10s | %d 次分析 | 最近: %s (%s)%n",
                    symbol, count, latest.get().getDate(), latest.get().getFinalSignal());
            }
        }
    }
    
    private void handleClear(String[] parts, MemoryService memoryService) {
        if (parts.length < 2) {
            System.out.print("\n确定要清空所有历史记录吗？(y/N): ");
            Scanner scanner = new Scanner(System.in);
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                memoryService.clearAllHistory();
                System.out.println("已清空所有历史记录");
            } else {
                System.out.println("已取消");
            }
        } else {
            String symbol = parts[1].toUpperCase();
            memoryService.clearHistory(symbol);
            System.out.println("已清空 " + symbol + " 的历史记录");
        }
    }
    
    private void printAnalysisResult(AgentState state, long duration) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("分析完成");
        System.out.println("=".repeat(60));
        
        System.out.println("\n基本信息:");
        System.out.println("  股票代码: " + state.getCompany());
        System.out.println("  分析日期: " + state.getDate());
        System.out.println("  执行时间: " + duration + " ms");
        
        System.out.println("\n分析统计:");
        System.out.println("  分析师报告: " + state.getAnalystReports().size() + " 份");
        System.out.println("  研究员观点: " + state.getResearcherViewpoints().size() + " 个");
        System.out.println("  反思记录: " + state.getReflections().size() + " 条");
        
        System.out.println("\n最终决策:");
        String signal = state.getFinalSignal() != null ? state.getFinalSignal() : "未生成";
        String emoji = getSignalEmoji(signal);
        System.out.println("  交易信号: " + emoji + " " + signal);
        
        if (state.getResearchManagerDecision() != null) {
            System.out.println("\n研究经理决策:");
            System.out.println(truncate(state.getResearchManagerDecision(), 200));
        }
        
        if (state.getTradingPlan() != null) {
            System.out.println("\n交易计划:");
            System.out.println(truncate(state.getTradingPlan(), 200));
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("提示: 使用 'history " + state.getCompany() + "' 查看历史记录");
    }
    
    private String getSignalEmoji(String signal) {
        switch (signal.toUpperCase()) {
            case "BUY": return "📈";
            case "SELL": return "📉";
            case "HOLD": return "⏸️";
            default: return "❓";
        }
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
    
    private void printBanner() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  _____ _____              _      ");
        System.out.println(" |_   _|_   _| __ __ _  __| | ___ ");
        System.out.println("   | |   | || '__/ _` |/ _` |/ _ \\");
        System.out.println("   | |   | || | | (_| | (_| |  __/");
        System.out.println("   |_|   |_||_|  \\__,_|\\__,_|\\___|");
        System.out.println();
        System.out.println("  多智能体交易决策系统 v" + VERSION);
        System.out.println("  作者: 山泽");
        System.out.println("=".repeat(60));
    }
    
    private void printHelp() {
        System.out.println("\n可用命令:");
        System.out.println("  analyze, a <代码> [日期]  - 分析指定股票");
        System.out.println("  history, h <代码>         - 查看历史记录");
        System.out.println("  list, l                   - 列出所有已分析股票");
        System.out.println("  clear [代码]              - 清空历史记录");
        System.out.println("  help, ?                   - 显示帮助信息");
        System.out.println("  version, v                - 显示版本信息");
        System.out.println("  exit, quit, q             - 退出程序");
        System.out.println("\n示例:");
        System.out.println("  analyze AAPL              - 分析苹果股票（今天）");
        System.out.println("  analyze TSLA 2024-01-15   - 分析特斯拉（指定日期）");
        System.out.println("  history AAPL              - 查看AAPL历史");
    }
    
    private void printVersion() {
        System.out.println("\nJTrade v" + VERSION);
        System.out.println("作者: 山泽");
    }
}
