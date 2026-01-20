package io.leavesfly.jtrade.service;

import io.leavesfly.jtrade.core.report.ReportWriter;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.graph.TradingGraph;
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
    
    private final TradingGraph tradingGraph;
    private final ReportWriter reportWriter;
    
    public TradingService(TradingGraph tradingGraph, ReportWriter reportWriter) {
        this.tradingGraph = tradingGraph;
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
        
        try {
            // 使用 TradingGraph 执行核心流程
            AgentState state = tradingGraph.propagate(symbol, date);
            
            // 写入报告（属于服务层职责）
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
            return AgentState.builder()
                    .company(symbol)
                    .date(date)
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
