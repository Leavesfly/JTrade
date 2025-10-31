package io.leavesfly.jtrade.core.report;

import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.core.state.RiskDebateState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 报告写入器
 * 
 * 负责将分析结果写入特定目录
 * 支持按股票代码组织目录结构
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class ReportWriter {
    
    private static final String DEFAULT_OUTPUT_DIR = "./reports";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final String baseOutputDir;
    
    public ReportWriter() {
        this.baseOutputDir = DEFAULT_OUTPUT_DIR;
    }
    
    public ReportWriter(String baseOutputDir) {
        this.baseOutputDir = baseOutputDir;
    }
    
    /**
     * 写入完整报告
     * 
     * @param state 智能体状态
     * @return 报告目录路径
     */
    public Path writeFullReport(AgentState state) {
        try {
            // 创建目录结构: reports/{symbol}/{date}/
            Path reportDir = createReportDirectory(state.getCompany(), state.getDate().toString());
            
            // 生成时间戳
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            
            // 1. 写入分析师报告
            writeAnalystReports(reportDir, timestamp, state.getAnalystReports());
            
            // 2. 写入研究员辩论
            writeResearcherDebate(reportDir, timestamp, state.getResearcherViewpoints());
            
            // 3. 写入研究经理决策
            writeResearchManagerDecision(reportDir, timestamp, state.getResearchManagerDecision());
            
            // 4. 写入交易计划
            writeTradingPlan(reportDir, timestamp, state.getTradingPlan());
            
            // 5. 写入风险辩论
            writeRiskDebate(reportDir, timestamp, state.getRiskDebate());
            
            // 6. 写入风险管理决策
            writeRiskManagerDecision(reportDir, timestamp, state.getRiskManagerDecision());
            
            // 7. 写入反思记录
            writeReflections(reportDir, timestamp, state.getReflections());
            
            // 8. 写入最终报告摘要
            writeFinalSummary(reportDir, timestamp, state);
            
            log.info("报告已写入: {}", reportDir.toAbsolutePath());
            return reportDir;
            
        } catch (IOException e) {
            log.error("写入报告失败", e);
            throw new RuntimeException("写入报告失败", e);
        }
    }
    
    /**
     * 创建报告目录
     */
    private Path createReportDirectory(String symbol, String date) throws IOException {
        Path reportDir = Paths.get(baseOutputDir, symbol, date);
        Files.createDirectories(reportDir);
        return reportDir;
    }
    
    /**
     * 写入分析师报告
     */
    private void writeAnalystReports(Path reportDir, String timestamp, List<String> reports) throws IOException {
        if (reports == null || reports.isEmpty()) {
            return;
        }
        
        Path file = reportDir.resolve(String.format("%s_analyst_reports.txt", timestamp));
        StringBuilder content = new StringBuilder();
        
        content.append("=" .repeat(80)).append("\n");
        content.append("分析师报告\n");
        content.append("=" .repeat(80)).append("\n\n");
        
        for (int i = 0; i < reports.size(); i++) {
            content.append(String.format("【分析师报告 %d】\n", i + 1));
            content.append("-".repeat(80)).append("\n");
            content.append(reports.get(i)).append("\n\n");
        }
        
        Files.writeString(file, content.toString());
        log.debug("已写入分析师报告: {}", file.getFileName());
    }
    
    /**
     * 写入研究员辩论
     */
    private void writeResearcherDebate(Path reportDir, String timestamp, List<String> viewpoints) throws IOException {
        if (viewpoints == null || viewpoints.isEmpty()) {
            return;
        }
        
        Path file = reportDir.resolve(String.format("%s_researcher_debate.txt", timestamp));
        StringBuilder content = new StringBuilder();
        
        content.append("=" .repeat(80)).append("\n");
        content.append("研究员辩论记录\n");
        content.append("=" .repeat(80)).append("\n\n");
        
        for (int i = 0; i < viewpoints.size(); i++) {
            String speaker = (i % 2 == 0) ? "多头研究员" : "空头研究员";
            content.append(String.format("【第 %d 轮 - %s】\n", (i / 2) + 1, speaker));
            content.append("-".repeat(80)).append("\n");
            content.append(viewpoints.get(i)).append("\n\n");
        }
        
        Files.writeString(file, content.toString());
        log.debug("已写入研究员辩论: {}", file.getFileName());
    }
    
    /**
     * 写入研究经理决策
     */
    private void writeResearchManagerDecision(Path reportDir, String timestamp, String decision) throws IOException {
        if (decision == null || decision.isEmpty()) {
            return;
        }
        
        Path file = reportDir.resolve(String.format("%s_research_manager_decision.txt", timestamp));
        StringBuilder content = new StringBuilder();
        
        content.append("=" .repeat(80)).append("\n");
        content.append("研究经理决策\n");
        content.append("=" .repeat(80)).append("\n\n");
        content.append(decision).append("\n");
        
        Files.writeString(file, content.toString());
        log.debug("已写入研究经理决策: {}", file.getFileName());
    }
    
    /**
     * 写入交易计划
     */
    private void writeTradingPlan(Path reportDir, String timestamp, String plan) throws IOException {
        if (plan == null || plan.isEmpty()) {
            return;
        }
        
        Path file = reportDir.resolve(String.format("%s_trading_plan.txt", timestamp));
        StringBuilder content = new StringBuilder();
        
        content.append("=" .repeat(80)).append("\n");
        content.append("交易执行计划\n");
        content.append("=" .repeat(80)).append("\n\n");
        content.append(plan).append("\n");
        
        Files.writeString(file, content.toString());
        log.debug("已写入交易计划: {}", file.getFileName());
    }
    
    /**
     * 写入风险辩论
     */
    private void writeRiskDebate(Path reportDir, String timestamp, RiskDebateState riskDebate) throws IOException {
        if (riskDebate == null) {
            return;
        }
        
        Path file = reportDir.resolve(String.format("%s_risk_debate.txt", timestamp));
        StringBuilder content = new StringBuilder();
        
        content.append("=" .repeat(80)).append("\n");
        content.append("风险辩论记录\n");
        content.append("=" .repeat(80)).append("\n\n");
        
        // 激进观点
        if (riskDebate.getAggressiveStrategies() != null && !riskDebate.getAggressiveStrategies().isEmpty()) {
            content.append("【激进风险分析】\n");
            content.append("-".repeat(80)).append("\n");
            for (String strategy : riskDebate.getAggressiveStrategies()) {
                content.append(strategy).append("\n\n");
            }
        }
        
        // 保守观点
        if (riskDebate.getConservativeStrategies() != null && !riskDebate.getConservativeStrategies().isEmpty()) {
            content.append("【保守风险分析】\n");
            content.append("-".repeat(80)).append("\n");
            for (String strategy : riskDebate.getConservativeStrategies()) {
                content.append(strategy).append("\n\n");
            }
        }
        
        // 中立观点
        if (riskDebate.getNeutralStrategies() != null && !riskDebate.getNeutralStrategies().isEmpty()) {
            content.append("【中立风险分析】\n");
            content.append("-".repeat(80)).append("\n");
            for (String strategy : riskDebate.getNeutralStrategies()) {
                content.append(strategy).append("\n\n");
            }
        }
        
        Files.writeString(file, content.toString());
        log.debug("已写入风险辩论: {}", file.getFileName());
    }
    
    /**
     * 写入风险管理决策
     */
    private void writeRiskManagerDecision(Path reportDir, String timestamp, String decision) throws IOException {
        if (decision == null || decision.isEmpty()) {
            return;
        }
        
        Path file = reportDir.resolve(String.format("%s_risk_manager_decision.txt", timestamp));
        StringBuilder content = new StringBuilder();
        
        content.append("=" .repeat(80)).append("\n");
        content.append("风险管理审批决策\n");
        content.append("=" .repeat(80)).append("\n\n");
        content.append(decision).append("\n");
        
        Files.writeString(file, content.toString());
        log.debug("已写入风险管理决策: {}", file.getFileName());
    }
    
    /**
     * 写入反思记录
     */
    private void writeReflections(Path reportDir, String timestamp, List<String> reflections) throws IOException {
        if (reflections == null || reflections.isEmpty()) {
            return;
        }
        
        Path file = reportDir.resolve(String.format("%s_reflections.txt", timestamp));
        StringBuilder content = new StringBuilder();
        
        content.append("=" .repeat(80)).append("\n");
        content.append("反思与学习记录\n");
        content.append("=" .repeat(80)).append("\n\n");
        
        for (int i = 0; i < reflections.size(); i++) {
            content.append(String.format("【反思 %d】\n", i + 1));
            content.append("-".repeat(80)).append("\n");
            content.append(reflections.get(i)).append("\n\n");
        }
        
        Files.writeString(file, content.toString());
        log.debug("已写入反思记录: {}", file.getFileName());
    }
    
    /**
     * 写入最终报告摘要
     */
    private void writeFinalSummary(Path reportDir, String timestamp, AgentState state) throws IOException {
        Path file = reportDir.resolve(String.format("%s_FINAL_SUMMARY.txt", timestamp));
        StringBuilder content = new StringBuilder();
        
        content.append("╔" + "═".repeat(78) + "╗\n");
        content.append("║" + center("JTrade 交易决策完整报告", 78) + "║\n");
        content.append("╚" + "═".repeat(78) + "╝\n\n");
        
        // 基本信息
        content.append("【基本信息】\n");
        content.append("-".repeat(80)).append("\n");
        content.append(String.format("股票代码: %s\n", state.getCompany()));
        content.append(String.format("分析日期: %s\n", state.getDate().format(DATE_FORMAT)));
        content.append(String.format("生成时间: %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        content.append("\n");
        
        // 决策流程摘要
        content.append("【决策流程摘要】\n");
        content.append("-".repeat(80)).append("\n");
        content.append(String.format("✓ 分析师报告: %d 份\n", state.getAnalystReports() != null ? state.getAnalystReports().size() : 0));
        content.append(String.format("✓ 研究员辩论: %d 轮\n", state.getResearcherViewpoints() != null ? state.getResearcherViewpoints().size() / 2 : 0));
        content.append(String.format("✓ 研究经理决策: %s\n", state.getResearchManagerDecision() != null ? "已完成" : "未完成"));
        content.append(String.format("✓ 交易计划: %s\n", state.getTradingPlan() != null ? "已生成" : "未生成"));
        
        if (state.getRiskDebate() != null) {
            int totalDebates = 0;
            if (state.getRiskDebate().getAggressiveStrategies() != null) {
                totalDebates += state.getRiskDebate().getAggressiveStrategies().size();
            }
            if (state.getRiskDebate().getConservativeStrategies() != null) {
                totalDebates += state.getRiskDebate().getConservativeStrategies().size();
            }
            if (state.getRiskDebate().getNeutralStrategies() != null) {
                totalDebates += state.getRiskDebate().getNeutralStrategies().size();
            }
            content.append(String.format("✓ 风险辩论: %d 个观点\n", totalDebates));
        }
        
        content.append(String.format("✓ 风险管理审批: %s\n", state.getRiskManagerDecision() != null ? "已审批" : "未审批"));
        content.append(String.format("✓ 反思记录: %d 条\n", state.getReflections() != null ? state.getReflections().size() : 0));
        content.append("\n");
        
        // 最终决策
        content.append("【最终交易决策】\n");
        content.append("-".repeat(80)).append("\n");
        String signal = state.getFinalSignal() != null ? state.getFinalSignal() : "未生成";
        String emoji = getSignalEmoji(signal);
        content.append(String.format("%s 交易信号: %s\n", emoji, signal));
        content.append("\n");
        
        // 关键决策点
        if (state.getResearchManagerDecision() != null) {
            content.append("【研究经理核心观点】\n");
            content.append("-".repeat(80)).append("\n");
            content.append(truncate(state.getResearchManagerDecision(), 500)).append("\n\n");
        }
        
        if (state.getTradingPlan() != null) {
            content.append("【交易执行要点】\n");
            content.append("-".repeat(80)).append("\n");
            content.append(truncate(state.getTradingPlan(), 500)).append("\n\n");
        }
        
        if (state.getRiskManagerDecision() != null) {
            content.append("【风险管理意见】\n");
            content.append("-".repeat(80)).append("\n");
            content.append(truncate(state.getRiskManagerDecision(), 500)).append("\n\n");
        }
        
        // 文件清单
        content.append("【详细报告文件】\n");
        content.append("-".repeat(80)).append("\n");
        content.append(String.format("1. %s_analyst_reports.txt        - 分析师报告\n", timestamp));
        content.append(String.format("2. %s_researcher_debate.txt      - 研究员辩论\n", timestamp));
        content.append(String.format("3. %s_research_manager_decision.txt - 研究经理决策\n", timestamp));
        content.append(String.format("4. %s_trading_plan.txt           - 交易计划\n", timestamp));
        content.append(String.format("5. %s_risk_debate.txt            - 风险辩论\n", timestamp));
        content.append(String.format("6. %s_risk_manager_decision.txt  - 风险管理决策\n", timestamp));
        content.append(String.format("7. %s_reflections.txt            - 反思记录\n", timestamp));
        content.append("\n");
        
        content.append("=" .repeat(80)).append("\n");
        content.append("报告生成完成 | JTrade Multi-Agent Trading System\n");
        content.append("=" .repeat(80)).append("\n");
        
        Files.writeString(file, content.toString());
        log.info("✓ 已生成最终摘要: {}", file.getFileName());
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
        return text.substring(0, maxLength) + "...(详见完整报告)";
    }
    
    private String center(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, width - text.length() - padding));
    }
    
    /**
     * 获取报告目录路径
     */
    public Path getReportDirectory(String symbol, String date) {
        return Paths.get(baseOutputDir, symbol, date);
    }
    
    /**
     * 设置输出目录
     */
    public String getBaseOutputDir() {
        return baseOutputDir;
    }
}
