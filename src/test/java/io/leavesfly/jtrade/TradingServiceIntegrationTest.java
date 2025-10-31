package io.leavesfly.jtrade;

import io.leavesfly.jtrade.config.LlmConfig;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 交易服务集成测试
 * 
 * 测试完整的交易决策流程
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootTest
public class TradingServiceIntegrationTest {
    
    @Autowired
    private TradingService tradingService;
    
    @Autowired
    private LlmConfig llmConfig;
    
    @Test
    public void testFullTradingWorkflow() {
        log.info("========================================");
        log.info("  测试完整交易决策流程");
        log.info("========================================");
        log.info("LLM提供商: {}", llmConfig.getProvider());
        log.info("深度思考模型: {}", llmConfig.getDeepThinkModelName());
        log.info("快速思考模型: {}", llmConfig.getQuickThinkModelName());
        log.info("");
        
        // 执行分析
        String company = "AAPL";
        LocalDate date = LocalDate.of(2024, 5, 10);
        
        AgentState result = tradingService.executeTradingWorkflow(company, date);
        
        // 验证结果
        assertNotNull(result, "结果不应为null");
        assertEquals(company, result.getCompany(), "公司代码应匹配");
        assertEquals(date, result.getDate(), "日期应匹配");
        
        // 验证分析师报告
        assertNotNull(result.getAnalystReports(), "分析师报告不应为null");
        assertFalse(result.getAnalystReports().isEmpty(), "应该有分析师报告");
        log.info("✓ 分析师报告数量: {}", result.getAnalystReports().size());
        
        // 验证研究员观点
        assertNotNull(result.getResearcherViewpoints(), "研究员观点不应为null");
        assertFalse(result.getResearcherViewpoints().isEmpty(), "应该有研究员观点");
        log.info("✓ 研究员观点数量: {}", result.getResearcherViewpoints().size());
        
        // 验证研究经理决策
        assertNotNull(result.getResearchManagerDecision(), "研究经理决策不应为null");
        log.info("✓ 研究经理决策已生成");
        
        // 验证交易计划
        assertNotNull(result.getTradingPlan(), "交易计划不应为null");
        log.info("✓ 交易计划已生成");
        
        // 验证最终信号
        assertNotNull(result.getFinalSignal(), "最终信号不应为null");
        assertTrue(
                result.getFinalSignal().equals("BUY") || 
                result.getFinalSignal().equals("SELL") || 
                result.getFinalSignal().equals("HOLD"),
                "最终信号应该是BUY、SELL或HOLD之一"
        );
        log.info("✓ 最终信号: {}", result.getFinalSignal());
        
        log.info("");
        log.info("========================================");
        log.info("  所有测试通过！");
        log.info("========================================");
    }
    
    @Test
    public void testMultipleStocks() {
        log.info("========================================");
        log.info("  测试多股票分析");
        log.info("========================================");
        
        String[] stocks = {"AAPL", "MSFT", "GOOGL"};
        LocalDate date = LocalDate.of(2024, 5, 10);
        
        for (String stock : stocks) {
            log.info("");
            log.info("分析股票: {}", stock);
            
            AgentState result = tradingService.executeTradingWorkflow(stock, date);
            
            assertNotNull(result);
            assertEquals(stock, result.getCompany());
            assertNotNull(result.getFinalSignal());
            
            log.info("  信号: {}", result.getFinalSignal());
        }
        
        log.info("");
        log.info("========================================");
        log.info("  多股票分析测试完成！");
        log.info("========================================");
    }
}
