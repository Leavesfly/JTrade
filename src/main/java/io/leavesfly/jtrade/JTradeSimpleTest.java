package io.leavesfly.jtrade;

import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.service.TradingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

/**
 * JTrade简单测试应用
 * 
 * 用于快速验证系统功能
 * 
 * @author 山泽
 */
@SpringBootApplication
@Profile("test")
public class JTradeSimpleTest {
    
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.run(JTradeSimpleTest.class, args);
    }
    
    @Bean
    public CommandLineRunner jTradeSimpleTestRunner(TradingService tradingService) {
        return args -> {
            System.out.println("=".repeat(80));
            System.out.println("JTrade 系统测试");
            System.out.println("=".repeat(80));
            System.out.println();
            
            String symbol = "AAPL";
            LocalDate date = LocalDate.now();
            
            System.out.println("测试参数：");
            System.out.println("  股票代码: " + symbol);
            System.out.println("  日期: " + date);
            System.out.println();
            
            try {
                // 执行完整流程
                AgentState result = tradingService.executeTradingWorkflow(symbol, date);
                
                // 打印报告
                System.out.println();
                tradingService.printDecisionReport(result);
                
                System.out.println();
                System.out.println("=".repeat(80));
                System.out.println("测试完成！");
                System.out.println("=".repeat(80));
                
            } catch (Exception e) {
                System.err.println("测试失败: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
