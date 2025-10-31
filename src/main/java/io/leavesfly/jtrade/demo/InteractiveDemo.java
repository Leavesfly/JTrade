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
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * 交互式演示程序
 * 
 * 提供用户友好的交互式界面，允许用户输入参数并查看分析结果
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class InteractiveDemo {
    
    public static void main(String[] args) {
        SpringApplication.run(InteractiveDemo.class, args);
    }
    
    @Bean
    public CommandLineRunner interactiveDemoRunner(TradingService tradingService) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            
            printWelcome();
            
            while (true) {
                try {
                    System.out.println("\n" + "=".repeat(80));
                    System.out.println("请输入分析参数");
                    System.out.println("=".repeat(80));
                    
                    // 获取股票代码
                    System.out.print("\n股票代码 (例如: AAPL, TSLA, 输入 'exit' 退出): ");
                    String symbol = scanner.nextLine().trim().toUpperCase();
                    
                    if ("EXIT".equals(symbol) || "QUIT".equals(symbol)) {
                        System.out.println("\n感谢使用 JTrade 系统，再见！");
                        break;
                    }
                    
                    if (symbol.isEmpty()) {
                        System.out.println("❌ 股票代码不能为空");
                        continue;
                    }
                    
                    // 获取日期
                    System.out.print("分析日期 (格式: YYYY-MM-DD, 直接回车使用今天): ");
                    String dateInput = scanner.nextLine().trim();
                    LocalDate date;
                    
                    if (dateInput.isEmpty()) {
                        date = LocalDate.now();
                    } else {
                        try {
                            date = LocalDate.parse(dateInput, DateTimeFormatter.ISO_LOCAL_DATE);
                        } catch (Exception e) {
                            System.out.println("❌ 日期格式错误，使用今天的日期");
                            date = LocalDate.now();
                        }
                    }
                    
                    // 确认参数
                    System.out.println("\n" + "-".repeat(80));
                    System.out.println("分析参数确认：");
                    System.out.println("  股票代码: " + symbol);
                    System.out.println("  分析日期: " + date);
                    System.out.println("-".repeat(80));
                    System.out.print("\n是否开始分析？(Y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    
                    if (!confirm.isEmpty() && !confirm.equals("y") && !confirm.equals("yes")) {
                        System.out.println("已取消分析");
                        continue;
                    }
                    
                    // 执行分析
                    System.out.println("\n🚀 开始分析...\n");
                    long startTime = System.currentTimeMillis();
                    
                    AgentState result = tradingService.executeTradingWorkflow(symbol, date);
                    
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    
                    // 显示结果
                    displayResults(result, duration);
                    
                    // 询问是否继续
                    System.out.print("\n继续分析其他股票？(Y/n): ");
                    String continueInput = scanner.nextLine().trim().toLowerCase();
                    
                    if (!continueInput.isEmpty() && !continueInput.equals("y") && !continueInput.equals("yes")) {
                        System.out.println("\n感谢使用 JTrade 系统，再见！");
                        break;
                    }
                    
                } catch (Exception e) {
                    log.error("分析过程出现错误", e);
                    System.out.println("\n❌ 分析失败: " + e.getMessage());
                    System.out.println("请重试或输入 'exit' 退出");
                }
            }
            
            scanner.close();
        };
    }
    
    private void printWelcome() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("欢迎使用 JTrade - 多智能体交易决策系统");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("系统功能：");
        System.out.println("  ✓ 多维度市场分析（技术面、基本面、新闻、情绪）");
        System.out.println("  ✓ 多空辩论机制");
        System.out.println("  ✓ 智能交易决策");
        System.out.println("  ✓ 风险管理审批");
        System.out.println();
        System.out.println("支持的股票市场：美股（US Stock Market）");
        System.out.println();
    }
    
    private void displayResults(AgentState state, long duration) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ 分析完成");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // 基本信息
        System.out.println("【基本信息】");
        System.out.println("  股票代码: " + state.getCompany());
        System.out.println("  分析日期: " + state.getDate());
        System.out.println("  执行时间: " + duration + " ms");
        System.out.println();
        
        // 分析摘要
        System.out.println("【分析摘要】");
        System.out.println("  分析师报告: " + state.getAnalystReports().size() + " 份");
        System.out.println("  研究员观点: " + state.getResearcherViewpoints().size() + " 个");
        System.out.println();
        
        // 最终决策
        System.out.println("【最终决策】");
        if (state.getFinalSignal() != null) {
            String signal = state.getFinalSignal();
            String emoji = getSignalEmoji(signal);
            System.out.println("  交易信号: " + emoji + " " + signal);
        } else {
            System.out.println("  交易信号: 未生成");
        }
        System.out.println();
        
        // 详细报告提示
        System.out.println("【详细报告】");
        if (state.getResearchManagerDecision() != null) {
            System.out.println("研究经理决策：");
            System.out.println(truncate(state.getResearchManagerDecision(), 200));
            System.out.println();
        }
        
        if (state.getTradingPlan() != null) {
            System.out.println("交易计划：");
            System.out.println(truncate(state.getTradingPlan(), 200));
            System.out.println();
        }
        
        System.out.println("=".repeat(80));
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
}
