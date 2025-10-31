package io.leavesfly.jtrade.agents.base;

/**
 * 智能体类型枚举
 * 
 * @author 山泽
 */
public enum AgentType {
    
    // 分析师团队
    MARKET_ANALYST("市场分析师"),
    SOCIAL_MEDIA_ANALYST("社交媒体分析师"),
    NEWS_ANALYST("新闻分析师"),
    FUNDAMENTALS_ANALYST("基本面分析师"),
    
    // 研究员团队
    BULL_RESEARCHER("多头研究员"),
    BEAR_RESEARCHER("空头研究员"),
    
    // 管理层
    RESEARCH_MANAGER("研究经理"),
    RISK_MANAGER("风险管理器"),
    
    // 交易员
    TRADER("交易员"),
    
    // 风险评估团队
    AGGRESSIVE_DEBATER("激进分析师"),
    CONSERVATIVE_DEBATER("保守分析师"),
    NEUTRAL_DEBATER("中立分析师"),
    
    // 新增：工具支持的通用智能体
    REC_AGENT("工具智能体");
    
    private final String displayName;
    
    AgentType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
