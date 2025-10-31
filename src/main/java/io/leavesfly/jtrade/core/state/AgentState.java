package io.leavesfly.jtrade.core.state;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主状态容器
 * 
 * 包含整个交易决策流程中的所有状态信息
 * 使用不可变对象模式确保状态安全性
 * 
 * @author 山泽
 */
@Data
@Builder(toBuilder = true)
public class AgentState {
    
    /**
     * 公司股票代码
     */
    private String company;
    
    /**
     * 交易日期
     */
    private LocalDate date;
    
    /**
     * 分析师报告列表
     */
    @Builder.Default
    private List<String> analystReports = new ArrayList<>();
    
    /**
     * 研究员观点列表
     */
    @Builder.Default
    private List<String> researcherViewpoints = new ArrayList<>();
    
    /**
     * 投资辩论状态
     */
    private InvestDebateState investDebate;
    
    /**
     * 研究经理决策
     */
    private String researchManagerDecision;
    
    /**
     * 交易计划
     */
    private String tradingPlan;
    
    /**
     * 风险辩论状态
     */
    private RiskDebateState riskDebate;
    
    /**
     * 风险管理器决策
     */
    private String riskManagerDecision;
    
    /**
     * 最终交易信号: BUY, SELL, HOLD
     */
    private String finalSignal;
    
    /**
     * 反思记录
     */
    @Builder.Default
    private List<String> reflections = new ArrayList<>();
    
    /**
     * 扩展属性
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * 添加分析师报告
     */
    public AgentState addAnalystReport(String report) {
        List<String> newReports = new ArrayList<>(this.analystReports);
        newReports.add(report);
        return this.toBuilder().analystReports(newReports).build();
    }
    
    /**
     * 添加研究员观点
     */
    public AgentState addResearcherViewpoint(String viewpoint) {
        List<String> newViewpoints = new ArrayList<>(this.researcherViewpoints);
        newViewpoints.add(viewpoint);
        return this.toBuilder().researcherViewpoints(newViewpoints).build();
    }
    
    /**
     * 添加反思记录
     */
    public AgentState addReflection(String reflection) {
        List<String> newReflections = new ArrayList<>(this.reflections);
        newReflections.add(reflection);
        return this.toBuilder().reflections(newReflections).build();
    }
    
    /**
     * 设置元数据
     */
    public AgentState putMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(this.metadata);
        newMetadata.put(key, value);
        return this.toBuilder().metadata(newMetadata).build();
    }
}
