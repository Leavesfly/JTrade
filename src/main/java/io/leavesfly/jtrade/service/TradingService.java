package io.leavesfly.jtrade.service;

import io.leavesfly.jtrade.agents.analysts.FundamentalsAnalyst;
import io.leavesfly.jtrade.agents.analysts.MarketAnalyst;
import io.leavesfly.jtrade.agents.analysts.NewsAnalyst;
import io.leavesfly.jtrade.agents.analysts.SocialMediaAnalyst;
import io.leavesfly.jtrade.agents.managers.ResearchManager;
import io.leavesfly.jtrade.agents.managers.RiskManager;
import io.leavesfly.jtrade.agents.researchers.BearResearcher;
import io.leavesfly.jtrade.agents.researchers.BullResearcher;
import io.leavesfly.jtrade.agents.risk.AggressiveDebator;
import io.leavesfly.jtrade.agents.risk.ConservativeDebator;
import io.leavesfly.jtrade.agents.risk.NeutralDebator;
import io.leavesfly.jtrade.agents.trader.Trader;
import io.leavesfly.jtrade.core.memory.MemoryService;
import io.leavesfly.jtrade.core.reflection.ReflectionService;
import io.leavesfly.jtrade.core.report.ReportWriter;
import io.leavesfly.jtrade.core.state.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * 交易服务
 * 
 * 协调各个智能体完成完整的交易决策流程
 * 
 * @author 山泽
 */
@Slf4j
@Service
public class TradingService {
    
    private final MarketAnalyst marketAnalyst;
    private final FundamentalsAnalyst fundamentalsAnalyst;
    private final NewsAnalyst newsAnalyst;
    private final SocialMediaAnalyst socialMediaAnalyst;
    private final BullResearcher bullResearcher;
    private final BearResearcher bearResearcher;
    private final ResearchManager researchManager;
    private final Trader trader;
    private final AggressiveDebator aggressiveDebator;
    private final ConservativeDebator conservativeDebator;
    private final NeutralDebator neutralDebator;
    private final RiskManager riskManager;
    private final ReflectionService reflectionService;
    private final MemoryService memoryService;
    private final ReportWriter reportWriter;
    
    public TradingService(
            MarketAnalyst marketAnalyst,
            FundamentalsAnalyst fundamentalsAnalyst,
            NewsAnalyst newsAnalyst,
            SocialMediaAnalyst socialMediaAnalyst,
            BullResearcher bullResearcher,
            BearResearcher bearResearcher,
            ResearchManager researchManager,
            Trader trader,
            AggressiveDebator aggressiveDebator,
            ConservativeDebator conservativeDebator,
            NeutralDebator neutralDebator,
            RiskManager riskManager,
            ReflectionService reflectionService,
            MemoryService memoryService,
            ReportWriter reportWriter) {
        this.marketAnalyst = marketAnalyst;
        this.fundamentalsAnalyst = fundamentalsAnalyst;
        this.newsAnalyst = newsAnalyst;
        this.socialMediaAnalyst = socialMediaAnalyst;
        this.bullResearcher = bullResearcher;
        this.bearResearcher = bearResearcher;
        this.researchManager = researchManager;
        this.trader = trader;
        this.aggressiveDebator = aggressiveDebator;
        this.conservativeDebator = conservativeDebator;
        this.neutralDebator = neutralDebator;
        this.riskManager = riskManager;
        this.reflectionService = reflectionService;
        this.memoryService = memoryService;
        this.reportWriter = reportWriter;
    }
    
    /**
     * 执行完整的交易决策流程
     * 
     * @param symbol 股票代码
     * @param date 交易日期
     * @return 最终状态
     */
    public AgentState executeTradingWorkflow(String symbol, LocalDate date) {
        log.info("=====================================");
        log.info("开始执行交易决策流程");
        log.info("股票代码: {}", symbol);
        log.info("交易日期: {}", date);
        log.info("=====================================");
        
        // 初始化状态
        AgentState state = AgentState.builder()
                .company(symbol)
                .date(date)
                .build();
        
        try {
            // 第一阶段：分析师团队分析
            log.info("\n【第一阶段：分析师团队分析】");
            state = marketAnalyst.execute(state);
            state = fundamentalsAnalyst.execute(state);
            state = newsAnalyst.execute(state);
            state = socialMediaAnalyst.execute(state);
            log.info("分析师报告数量: {}", state.getAnalystReports().size());
            
            // 第二阶段：研究员团队辩论
            log.info("\n【第二阶段：研究员团队辩论】");
            state = bullResearcher.execute(state);
            state = bearResearcher.execute(state);
            log.info("研究员观点数量: {}", state.getResearcherViewpoints().size());
            
            // 第三阶段：研究经理决策
            log.info("\n【第三阶段：研究经理决策】");
            state = researchManager.execute(state);
            log.info("研究经理决策完成");
            
            // 第四阶段：交易员制定计划
            log.info("\n【第四阶段：交易员制定计划】");
            state = trader.execute(state);
            log.info("交易计划制定完成");
            
            // 第五阶段：风险辩论（新增）
            log.info("\n【第五阶段：风险辩论】");
            state = aggressiveDebator.execute(state);
            state = conservativeDebator.execute(state);
            state = neutralDebator.execute(state);
            log.info("风险辩论完成");
            
            // 第六阶段：风险管理审批
            log.info("\n【第六阶段：风险管理审批】");
            state = riskManager.execute(state);
            log.info("风险管理决策完成");
            
            // 第七阶段：反思与学习
            log.info("\n【第七阶段：反思与学习】");
            state = reflectionService.reflectOnAnalysis(state);
            state = reflectionService.reflectOnDecision(state);
            state = reflectionService.comprehensiveReflection(state);
            log.info("反思完成，反思记录: {} 条", state.getReflections().size());
            
            // 第八阶段：保存记忆
            log.info("\n【第八阶段：保存记忆】");
            memoryService.saveDecision(state);
            log.info("记忆保存完成");
            
            // 第九阶段：写入报告
            log.info("\n【第九阶段：写入报告】");
            Path reportDir = reportWriter.writeFullReport(state);
            log.info("报告已写入: {}", reportDir.toAbsolutePath());
            
            // 输出最终结果
            log.info("\n=====================================" );
            log.info("交易决策流程完成");
            log.info("最终信号: {}", state.getFinalSignal());
            log.info("报告目录: {}", reportDir.toAbsolutePath());
            log.info("=====================================");
            
            return state;
            
        } catch (Exception e) {
            log.error("交易决策流程执行失败", e);
            return state.toBuilder()
                    .finalSignal("ERROR")
                    .build();
        }
    }
    
    /**
     * 打印完整决策报告
     */
    public void printDecisionReport(AgentState state) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("交易决策报告");
        System.out.println("=".repeat(80));
        System.out.println("股票代码: " + state.getCompany());
        System.out.println("交易日期: " + state.getDate());
        System.out.println();
        
        System.out.println("【分析师报告】");
        System.out.println("-".repeat(80));
        for (String report : state.getAnalystReports()) {
            System.out.println(report);
            System.out.println();
        }
        
        System.out.println("【研究员观点】");
        System.out.println("-".repeat(80));
        for (String viewpoint : state.getResearcherViewpoints()) {
            System.out.println(viewpoint);
            System.out.println();
        }
        
        System.out.println("【研究经理决策】");
        System.out.println("-".repeat(80));
        System.out.println(state.getResearchManagerDecision());
        System.out.println();
        
        System.out.println("【交易计划】");
        System.out.println("-".repeat(80));
        System.out.println(state.getTradingPlan());
        System.out.println();
        
        System.out.println("【风险管理决策】");
        System.out.println("-".repeat(80));
        System.out.println(state.getRiskManagerDecision());
        System.out.println();
        
        System.out.println("【最终信号】");
        System.out.println("-".repeat(80));
        System.out.println(">>> " + state.getFinalSignal() + " <<<");
        System.out.println("=".repeat(80));
    }
}
