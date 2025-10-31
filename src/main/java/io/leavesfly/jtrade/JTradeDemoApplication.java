package io.leavesfly.jtrade;

import io.leavesfly.jtrade.core.memory.MemoryService;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.graph.TradingGraph;
import io.leavesfly.jtrade.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

/**
 * JTrade演示程序
 * 
 * 展示完整的交易决策流程
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
public class JTradeDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JTradeDemoApplication.class, args);
    }
    
    /**
     * 交互式演示
     * 
     * 演示如何使用JTrade进行交易决策分析
     */
    @Bean
    public CommandLineRunner demo(TradingService tradingService, 
                                   TradingGraph tradingGraph,
                                   MemoryService memoryService) {
        return args -> {
            log.info("========================================");
            log.info("  JTrade - 多智能体交易决策系统演示");
            log.info("========================================");
            
            // 检查是否提供了命令行参数
            String company = "NVDA";  // 默认股票
            LocalDate date = LocalDate.of(2024, 5, 10);  // 默认日期
            
            if (args.length >= 1) {
                company = args[0];
            }
            if (args.length >= 2) {
                date = LocalDate.parse(args[1]);
            }
            
            log.info("");
            log.info("分析目标:");
            log.info("  股票代码: {}", company);
            log.info("  分析日期: {}", date);
            log.info("");
            
            // 执行分析
            AgentState result = tradingService.executeTradingWorkflow(company, date);
            
            // 输出结果
            printResults(result);
        };
    }
    
    /**
     * 打印分析结果
     */
    private void printResults(AgentState state) {
        log.info("");
        log.info("========================================");
        log.info("  分析结果汇总");
        log.info("========================================");
        log.info("");
        
        // 分析师报告
        log.info("--- 分析师报告 ({} 份) ---", state.getAnalystReports().size());
        for (int i = 0; i < state.getAnalystReports().size(); i++) {
            log.info("");
            log.info("报告 {}:", i + 1);
            log.info("{}", truncate(state.getAnalystReports().get(i), 200));
        }
        
        // 研究员观点
        if (!state.getResearcherViewpoints().isEmpty()) {
            log.info("");
            log.info("--- 研究员辩论 ({} 轮) ---", state.getResearcherViewpoints().size());
            for (int i = 0; i < state.getResearcherViewpoints().size(); i++) {
                log.info("");
                log.info("观点 {}:", i + 1);
                log.info("{}", truncate(state.getResearcherViewpoints().get(i), 200));
            }
        }
        
        // 研究经理决策
        if (state.getResearchManagerDecision() != null) {
            log.info("");
            log.info("--- 研究经理决策 ---");
            log.info("{}", truncate(state.getResearchManagerDecision(), 300));
        }
        
        // 交易计划
        if (state.getTradingPlan() != null) {
            log.info("");
            log.info("--- 交易执行计划 ---");
            log.info("{}", truncate(state.getTradingPlan(), 300));
        }
        
        // 最终信号
        log.info("");
        log.info("========================================");
        log.info("  最终交易信号: {}", state.getFinalSignal());
        log.info("========================================");
        log.info("");
    }
    
    /**
     * 截断长文本用于显示
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
