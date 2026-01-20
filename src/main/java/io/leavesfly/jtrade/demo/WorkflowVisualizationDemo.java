package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.agents.analysts.FundamentalsAnalyst;
import io.leavesfly.jtrade.agents.analysts.MarketAnalyst;
import io.leavesfly.jtrade.agents.analysts.NewsAnalyst;
import io.leavesfly.jtrade.agents.analysts.SocialMediaAnalyst;
import io.leavesfly.jtrade.agents.managers.ResearchManager;
import io.leavesfly.jtrade.agents.managers.RiskManager;
import io.leavesfly.jtrade.agents.researchers.BearResearcher;
import io.leavesfly.jtrade.agents.researchers.BullResearcher;
import io.leavesfly.jtrade.agents.trader.Trader;
import io.leavesfly.jtrade.core.state.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDate;

/**
 * 工作流可视化演示
 * 
 * 可视化展示多智能体协作的完整工作流程
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class WorkflowVisualizationDemo {
    
    public static void main(String[] args) {
        SpringApplication.run(WorkflowVisualizationDemo.class, args);
    }
    
    @Bean
    public CommandLineRunner workflowVisualizationRunner(
            MarketAnalyst marketAnalyst,
            FundamentalsAnalyst fundamentalsAnalyst,
            NewsAnalyst newsAnalyst,
            SocialMediaAnalyst socialMediaAnalyst,
            BullResearcher bullResearcher,
            BearResearcher bearResearcher,
            ResearchManager researchManager,
            Trader trader,
            RiskManager riskManager) {
        
        return args -> {
            printWorkflowDiagram();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("开始执行工作流演示");
            System.out.println("=".repeat(80));
            System.out.println();
            
            // 初始化状态
            AgentState state = AgentState.builder()
                    .company("TSLA")
                    .date(LocalDate.now())
                    .build();
            
            System.out.println("目标股票: " + state.getCompany());
            System.out.println("分析日期: " + state.getDate());
            System.out.println();
            
            // 阶段1：分析师团队
            printPhaseHeader("阶段 1", "分析师团队分析", "🔍");
            state = executeAgent(marketAnalyst, state, "1/4");
            state = executeAgent(fundamentalsAnalyst, state, "2/4");
            state = executeAgent(newsAnalyst, state, "3/4");
            state = executeAgent(socialMediaAnalyst, state, "4/4");
            printPhaseSummary("分析师报告", state.getAnalystReports().size());
            
            // 阶段2：研究员团队
            printPhaseHeader("阶段 2", "研究员团队辩论", "💭");
            state = executeAgent(bullResearcher, state, "1/2");
            state = executeAgent(bearResearcher, state, "2/2");
            printPhaseSummary("研究员观点", state.getResearcherViewpoints().size());
            
            // 阶段3：研究经理
            printPhaseHeader("阶段 3", "研究经理决策", "🎯");
            state = executeAgent(researchManager, state, "1/1");
            printPhaseSummary("决策", state.getResearchManagerDecision() != null ? 1 : 0);
            
            // 阶段4：交易员
            printPhaseHeader("阶段 4", "交易员制定计划", "📊");
            state = executeAgent(trader, state, "1/1");
            printPhaseSummary("交易计划", state.getTradingPlan() != null ? 1 : 0);
            
            // 阶段5：风险管理
            printPhaseHeader("阶段 5", "风险管理审批", "🛡️");
            state = executeAgent(riskManager, state, "1/1");
            printPhaseSummary("最终信号", state.getFinalSignal() != null ? 1 : 0);
            
            // 最终结果
            printFinalResult(state);
        };
    }
    
    private void printWorkflowDiagram() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("JTrade 工作流程图");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("                    ┌─────────────────┐");
        System.out.println("                    │   开始分析      │");
        System.out.println("                    └────────┬────────┘");
        System.out.println("                             │");
        System.out.println("                             ▼");
        System.out.println("        ┌────────────────────────────────────────┐");
        System.out.println("        │   阶段1: 分析师团队 (4个并行)         │");
        System.out.println("        ├────────────────────────────────────────┤");
        System.out.println("        │ • 市场分析师  • 基本面分析师           │");
        System.out.println("        │ • 新闻分析师  • 社交媒体分析师         │");
        System.out.println("        └──────────────────┬─────────────────────┘");
        System.out.println("                           │");
        System.out.println("                           ▼");
        System.out.println("        ┌────────────────────────────────────────┐");
        System.out.println("        │   阶段2: 研究员团队辩论               │");
        System.out.println("        ├────────────────────────────────────────┤");
        System.out.println("        │ • 多头研究员  VS  空头研究员           │");
        System.out.println("        └──────────────────┬─────────────────────┘");
        System.out.println("                           │");
        System.out.println("                           ▼");
        System.out.println("        ┌────────────────────────────────────────┐");
        System.out.println("        │   阶段3: 研究经理决策                 │");
        System.out.println("        ├────────────────────────────────────────┤");
        System.out.println("        │ • 综合分析  • 做出投资方向决策         │");
        System.out.println("        └──────────────────┬─────────────────────┘");
        System.out.println("                           │");
        System.out.println("                           ▼");
        System.out.println("        ┌────────────────────────────────────────┐");
        System.out.println("        │   阶段4: 交易员制定计划               │");
        System.out.println("        ├────────────────────────────────────────┤");
        System.out.println("        │ • 具体交易策略  • 仓位管理             │");
        System.out.println("        └──────────────────┬─────────────────────┘");
        System.out.println("                           │");
        System.out.println("                           ▼");
        System.out.println("        ┌────────────────────────────────────────┐");
        System.out.println("        │   阶段5: 风险管理审批                 │");
        System.out.println("        ├────────────────────────────────────────┤");
        System.out.println("        │ • 风险评估  • 最终批准/拒绝            │");
        System.out.println("        └──────────────────┬─────────────────────┘");
        System.out.println("                           │");
        System.out.println("                           ▼");
        System.out.println("                    ┌─────────────────┐");
        System.out.println("                    │  输出交易信号   │");
        System.out.println("                    └─────────────────┘");
        System.out.println();
    }
    
    private void printPhaseHeader(String phase, String description, String emoji) {
        System.out.println("\n" + "━".repeat(80));
        System.out.println(emoji + "  " + phase + ": " + description);
        System.out.println("━".repeat(80));
    }
    
    private AgentState executeAgent(io.leavesfly.jtrade.agents.base.Agent agent, 
                                      AgentState state, String progress) {
        long startTime = System.currentTimeMillis();
        System.out.print("  [" + progress + "] 执行 " + agent.getName() + "...");
        
        try {
            AgentState newState = agent.execute(state);
            long duration = System.currentTimeMillis() - startTime;
            System.out.println(" ✓ 完成 (" + duration + " ms)");
            return newState;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println(" ✗ 失败 (" + duration + " ms): " + e.getMessage());
            return state;
        }
    }
    
    private void printPhaseSummary(String itemName, int count) {
        System.out.println("  ─────────────────────────────────────");
        System.out.println("  📝 生成 " + itemName + ": " + count + " 个");
        System.out.println();
    }
    
    private void printFinalResult(AgentState state) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎉 工作流执行完成");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("最终结果：");
        System.out.println("  股票代码: " + state.getCompany());
        System.out.println("  最终信号: " + (state.getFinalSignal() != null ? 
                "【" + state.getFinalSignal() + "】" : "未生成"));
        System.out.println();
        System.out.println("数据统计：");
        System.out.println("  • 分析师报告: " + state.getAnalystReports().size() + " 份");
        System.out.println("  • 研究员观点: " + state.getResearcherViewpoints().size() + " 个");
        System.out.println("  • 研究经理决策: " + (state.getResearchManagerDecision() != null ? "已完成" : "未完成"));
        System.out.println("  • 交易计划: " + (state.getTradingPlan() != null ? "已制定" : "未制定"));
        System.out.println("  • 风险管理决策: " + (state.getRiskManagerDecision() != null ? "已完成" : "未完成"));
        System.out.println();
        System.out.println("=".repeat(80));
    }
}
