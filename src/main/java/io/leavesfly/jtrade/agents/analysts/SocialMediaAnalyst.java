package io.leavesfly.jtrade.agents.analysts;

import io.leavesfly.jtrade.agents.base.BaseRecAgent;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.agents.base.AgentType;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
import io.leavesfly.jtrade.llm.client.LlmClient;
import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 社交媒体分析师
 * 
 * 负责分析社交媒体情绪和舆论
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class SocialMediaAnalyst extends BaseRecAgent {

    public SocialMediaAnalyst(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        return super.execute(state);
    }
    
    private String buildPrompt(String symbol, Map<String, Object> sentiment) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下关于 ").append(symbol).append(" 的社交媒体情绪数据：\n\n");
        
        sb.append("情绪指标：\n");
        sentiment.forEach((key, value) -> 
            sb.append("- ").append(key).append(": ").append(value).append("\n")
        );
        
        sb.append("\n请基于这些社交媒体数据：\n");
        sb.append("1. 分析散户投资者的整体情绪倾向\n");
        sb.append("2. 评估社交媒体热度和关注度的变化趋势\n");
        sb.append("3. 识别可能影响股价的舆论热点\n");
        sb.append("4. 判断散户情绪是否可能引发短期价格波动\n");
        sb.append("5. 给出基于情绪分析的投资建议（买入/卖出/观望）\n");
        sb.append("\n请提供专业、简洁的分析报告。");
        
        return sb.toString();
    }
    
    @Override
    public String getName() {
        return "社交媒体分析师";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.SOCIAL_MEDIA_ANALYST;
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.analyst.social.system 和 react.analyst.social.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.analyst.social";
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位资深的社交媒体分析师，擅长分析散户情绪和市场舆论。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String symbol = state.getCompany();
        String dateStr = state.getDate() != null ? state.getDate().toString() : "N/A";
        return String.format(
                "目标：对 %s 在 %s 的社交媒体情绪进行分析，评估舆论热点、情绪倾向与潜在短期波动，并给出投资建议（BUY/SELL/HOLD）。必要时调用工具。\n" +
                "请严格使用如下格式进行交互：\nThought: ...\nAction: <tool_name>\nAction Input: {json}\n在获得我返回的 Observation 后继续，直到给出 Final Answer。\n初始上下文：symbol=%s, date=%s",
                symbol, dateStr, symbol, dateStr
        );
    }
}
