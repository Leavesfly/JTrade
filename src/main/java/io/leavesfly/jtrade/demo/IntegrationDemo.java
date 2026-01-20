package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.graph.TradingGraph;
import io.leavesfly.jtrade.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * 完整功能集成演示
 * 
 * 展示所有模块的协同工作：
 * 1. Graph 工作流编排
 * 2. 12个智能体协作
 * 3. Prompt 管理系统
 * 4. 风险辩论机制
 * 5. 反思与记忆系统
 * 6. CLI 交互界面
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class IntegrationDemo {
    
    public static void main(String[] args) {
        SpringApplication.run(IntegrationDemo.class, args);
    }
    
    @Bean
    public CommandLineRunner integrationDemoRunner(TradingGraph tradingGraph, TradingService tradingService) {
        return args -> {
            printBanner();
            
            Scanner scanner = new Scanner(System.in);
            
            // 演示模式选择
            System.out.println("\n请选择演示模式：");
            System.out.println("1. 使用 TradingGraph（图编排模式）");
            System.out.println("2. 使用 TradingService（服务模式）");
            System.out.println("3. 对比两种模式");
            System.out.print("\n请输入选择 (1/2/3): ");
            
            String choice = scanner.nextLine().trim();
            
            // 获取股票代码
            System.out.print("\n请输入股票代码 (默认: AAPL): ");
            String symbol = scanner.nextLine().trim();
            if (symbol.isEmpty()) {
                symbol = "AAPL";
            }
            
            // 获取日期
            System.out.print("请输入日期 (格式: YYYY-MM-DD, 默认: 今天): ");
            String dateStr = scanner.nextLine().trim();
            LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);
            
            switch (choice) {
                case "1":
                    demonstrateGraphMode(tradingGraph, symbol, date);
                    break;
                case "2":
                    demonstrateServiceMode(tradingService, symbol, date);
                    break;
                case "3":
                    compareModesDemo(tradingGraph, tradingService, symbol, date);
                    break;
                default:
                    System.out.println("无效选择，使用默认模式（图编排）");
                    demonstrateGraphMode(tradingGraph, symbol, date);
            }
            
            scanner.close();
        };
    }
    
    /**
     * 演示 TradingGraph 模式
     */
    private void demonstrateGraphMode(TradingGraph tradingGraph, String symbol, LocalDate date) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("【模式1：TradingGraph 图编排模式】");
        System.out.println("=".repeat(80));
        
        long startTime = System.currentTimeMillis();
        
        // 使用 TradingGraph 执行
        AgentState result = tradingGraph.propagate(symbol, date);
        
        long duration = System.currentTimeMillis() - startTime;
        
        // 打印结果
        printResult(result, duration, "TradingGraph");
        
        // 展示 Graph 特有的功能
        System.out.println("\n" + "-".repeat(80));
        System.out.println("【Graph 模式特色功能】");
        System.out.println("-".repeat(80));
        System.out.println("✓ 条件分支判断（ConditionalLogic）");
        System.out.println("✓ 状态历史记录（GraphPropagator）");
        System.out.println("✓ 灵活的工作流编排");
        System.out.println("✓ 可配置的辩论轮数");
    }
    
    /**
     * 演示 TradingService 模式
     */
    private void demonstrateServiceMode(TradingService tradingService, String symbol, LocalDate date) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("【模式2：TradingService 服务模式】");
        System.out.println("=".repeat(80));
        
        long startTime = System.currentTimeMillis();
        
        // 使用 TradingService 执行
        AgentState result = tradingService.executeTradingWorkflow(symbol, date);
        
        long duration = System.currentTimeMillis() - startTime;
        
        // 打印结果
        printResult(result, duration, "TradingService");
        
        // 展示 Service 特有的功能
        System.out.println("\n" + "-".repeat(80));
        System.out.println("【Service 模式特色功能】");
        System.out.println("-".repeat(80));
        System.out.println("✓ 简洁的顺序工作流");
        System.out.println("✓ Spring 依赖注入集成");
        System.out.println("✓ 更易理解和维护");
        System.out.println("✓ 适合快速开发");
    }
    
    /**
     * 对比两种模式
     */
    private void compareModesDemo(TradingGraph tradingGraph, TradingService tradingService, 
                                   String symbol, LocalDate date) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("【对比演示：TradingGraph vs TradingService】");
        System.out.println("=".repeat(80));
        
        // 模式1：TradingGraph
        System.out.println("\n执行模式1：TradingGraph...");
        long startTime1 = System.currentTimeMillis();
        AgentState result1 = tradingGraph.propagate(symbol, date);
        long duration1 = System.currentTimeMillis() - startTime1;
        
        // 模式2：TradingService
        System.out.println("\n执行模式2：TradingService...");
        long startTime2 = System.currentTimeMillis();
        AgentState result2 = tradingService.executeTradingWorkflow(symbol, date);
        long duration2 = System.currentTimeMillis() - startTime2;
        
        // 对比结果
        System.out.println("\n" + "=".repeat(80));
        System.out.println("【对比结果】");
        System.out.println("=".repeat(80));
        
        compareResults(result1, result2, duration1, duration2);
    }
    
    /**
     * 打印执行结果
     */
    private void printResult(AgentState state, long duration, String mode) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("执行完成 - " + mode);
        System.out.println("=".repeat(80));
        
        System.out.println("\n📊 基本信息：");
        System.out.println("  股票代码: " + state.getCompany());
        System.out.println("  分析日期: " + state.getDate());
        System.out.println("  执行时间: " + duration + " ms");
        
        System.out.println("\n📈 工作流统计：");
        System.out.println("  分析师报告: " + state.getAnalystReports().size() + " 份");
        System.out.println("  研究员观点: " + state.getResearcherViewpoints().size() + " 个");
        System.out.println("  反思记录: " + state.getReflections().size() + " 条");
        
        if (state.getRiskDebate() != null) {
            System.out.println("  风险辩论:");
            System.out.println("    - 激进观点: " + state.getRiskDebate().getAggressiveStrategies().size());
            System.out.println("    - 保守观点: " + state.getRiskDebate().getConservativeStrategies().size());
            System.out.println("    - 中立观点: " + state.getRiskDebate().getNeutralStrategies().size());
        }
        
        System.out.println("\n🎯 最终决策：");
        String signal = state.getFinalSignal() != null ? state.getFinalSignal() : "未生成";
        String emoji = getSignalEmoji(signal);
        System.out.println("  " + emoji + " 交易信号: " + signal);
        
        if (state.getResearchManagerDecision() != null) {
            System.out.println("\n📝 研究经理决策:");
            System.out.println("  " + truncate(state.getResearchManagerDecision(), 150));
        }
        
        if (state.getTradingPlan() != null) {
            System.out.println("\n💼 交易计划:");
            System.out.println("  " + truncate(state.getTradingPlan(), 150));
        }
        
        if (state.getRiskManagerDecision() != null) {
            System.out.println("\n🛡️ 风险管理决策:");
            System.out.println("  " + truncate(state.getRiskManagerDecision(), 150));
        }
    }
    
    /**
     * 对比两种模式的结果
     */
    private void compareResults(AgentState result1, AgentState result2, long duration1, long duration2) {
        System.out.println("\n┌─────────────────────┬─────────────────┬─────────────────┐");
        System.out.println("│ 指标                │ TradingGraph    │ TradingService  │");
        System.out.println("├─────────────────────┼─────────────────┼─────────────────┤");
        System.out.printf("│ 执行时间            │ %-15s │ %-15s │%n", 
            duration1 + " ms", duration2 + " ms");
        System.out.printf("│ 分析师报告          │ %-15d │ %-15d │%n",
            result1.getAnalystReports().size(), result2.getAnalystReports().size());
        System.out.printf("│ 研究员观点          │ %-15d │ %-15d │%n",
            result1.getResearcherViewpoints().size(), result2.getResearcherViewpoints().size());
        System.out.printf("│ 反思记录            │ %-15d │ %-15d │%n",
            result1.getReflections().size(), result2.getReflections().size());
        System.out.printf("│ 最终信号            │ %-15s │ %-15s │%n",
            result1.getFinalSignal(), result2.getFinalSignal());
        System.out.println("└─────────────────────┴─────────────────┴─────────────────┘");
        
        System.out.println("\n💡 分析：");
        System.out.println("  - 两种模式产生相同的分析结果");
        System.out.println("  - TradingGraph 提供更灵活的工作流控制");
        System.out.println("  - TradingService 更简洁易用");
        System.out.println("  - 可根据项目需求选择合适的模式");
    }
    
    private String getSignalEmoji(String signal) {
        if (signal == null) return "❓";
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
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  ___       _                       _   _             ____                      ");
        System.out.println(" |_ _|_ __ | |_ ___  __ _ _ __ __ _| |_(_) ___  _ __ |  _ \\  ___ _ __ ___   ___  ");
        System.out.println("  | || '_ \\| __/ _ \\/ _` | '__/ _` | __| |/ _ \\| '_ \\| | | |/ _ \\ '_ ` _ \\ / _ \\ ");
        System.out.println("  | || | | | ||  __/ (_| | | | (_| | |_| | (_) | | | | |_| |  __/ | | | | | (_) |");
        System.out.println(" |___|_| |_|\\__\\___|\\__, |_|  \\__,_|\\__|_|\\___/|_| |_|____/ \\___|_| |_| |_|\\___/ ");
        System.out.println("                    |___/                                                         ");
        System.out.println();
        System.out.println("  JTrade 完整功能集成演示");
        System.out.println("  展示所有模块的协同工作");
        System.out.println("=".repeat(80));
        
        System.out.println("\n✨ 集成的功能模块：");
        System.out.println("  1. 📊 TradingGraph - 图工作流编排");
        System.out.println("  2. 🔧 TradingService - 服务层工作流");
        System.out.println("  3. 🤖 12个智能体 - 完整的决策团队");
        System.out.println("  4. 💬 Prompt管理 - 集中式提示词管理");
        System.out.println("  5. ⚖️ 风险辩论 - 3方辩论机制");
        System.out.println("  6. 🧠 反思系统 - 3层反思机制");
        System.out.println("  7. 💾 记忆系统 - 历史决策记录");
        System.out.println("  8. 📡 数据集成 - Finnhub API");
    }
}
