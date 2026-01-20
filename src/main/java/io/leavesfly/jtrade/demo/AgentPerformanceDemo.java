package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.agents.base.Agent;
import io.leavesfly.jtrade.core.state.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDate;
import java.util.List;

/**
 * 智能体性能分析演示
 * 
 * 展示各个智能体的执行时间和性能特征
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class AgentPerformanceDemo {
    
    public static void main(String[] args) {
        SpringApplication.run(AgentPerformanceDemo.class, args);
    }
    
    @Bean
    public CommandLineRunner agentPerformanceRunner(List<Agent> agents) {
        return args -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("智能体性能分析演示");
            System.out.println("=".repeat(80));
            System.out.println();
            
            // 初始化状态
            AgentState state = AgentState.builder()
                    .company("AAPL")
                    .date(LocalDate.now())
                    .build();
            
            System.out.println("测试参数：");
            System.out.println("  股票代码: " + state.getCompany());
            System.out.println("  测试日期: " + state.getDate());
            System.out.println("  智能体数量: " + agents.size());
            System.out.println();
            
            System.out.println("-".repeat(80));
            System.out.printf("%-30s %-15s %-20s %-15s%n", 
                    "智能体名称", "类型", "执行时间(ms)", "状态");
            System.out.println("-".repeat(80));
            
            long totalTime = 0;
            int successCount = 0;
            int failureCount = 0;
            
            // 测试每个智能体
            for (Agent agent : agents) {
                long startTime = System.currentTimeMillis();
                String status = "SUCCESS";
                
                try {
                    state = agent.execute(state);
                    successCount++;
                } catch (Exception e) {
                    status = "FAILED: " + e.getMessage();
                    failureCount++;
                }
                
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                totalTime += executionTime;
                
                System.out.printf("%-30s %-15s %-20d %-15s%n",
                        agent.getName(),
                        agent.getType().name(),
                        executionTime,
                        status);
            }
            
            System.out.println("-".repeat(80));
            System.out.println();
            
            // 性能统计
            System.out.println("性能统计：");
            System.out.println("  总执行时间: " + totalTime + " ms");
            System.out.println("  平均执行时间: " + (agents.isEmpty() ? 0 : totalTime / agents.size()) + " ms");
            System.out.println("  成功数量: " + successCount);
            System.out.println("  失败数量: " + failureCount);
            System.out.println("  成功率: " + String.format("%.2f%%", 
                    agents.isEmpty() ? 0 : (successCount * 100.0 / agents.size())));
            System.out.println();
            
            // 输出状态统计
            System.out.println("状态统计：");
            System.out.println("  分析师报告数: " + state.getAnalystReports().size());
            System.out.println("  研究员观点数: " + state.getResearcherViewpoints().size());
            System.out.println("  研究经理决策: " + (state.getResearchManagerDecision() != null ? "已完成" : "未完成"));
            System.out.println("  交易计划: " + (state.getTradingPlan() != null ? "已制定" : "未制定"));
            System.out.println("  最终信号: " + (state.getFinalSignal() != null ? state.getFinalSignal() : "无"));
            System.out.println();
            
            System.out.println("=".repeat(80));
            System.out.println("性能分析完成");
            System.out.println("=".repeat(80));
        };
    }
}
