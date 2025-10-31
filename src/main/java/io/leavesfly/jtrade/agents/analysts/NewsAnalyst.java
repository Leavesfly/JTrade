package io.leavesfly.jtrade.agents.analysts;

import io.leavesfly.jtrade.agents.base.BaseRecAgent;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.agents.base.AgentType;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.dataflow.model.NewsData;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
import io.leavesfly.jtrade.llm.client.LlmClient;
import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻分析师
 * 
 * 负责分析市场新闻和事件影响
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class NewsAnalyst extends BaseRecAgent {
    
    // Deleted: moved to BaseRecAgent
    // Deleted: moved to BaseRecAgent
    // Deleted: moved to BaseRecAgent
    
    public NewsAnalyst(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        return super.execute(state);
    }
    
    private String buildPrompt(String symbol, List<NewsData> newsList) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下关于 ").append(symbol).append(" 的最新新闻：\n\n");
        
        int count = 1;
        for (NewsData news : newsList) {
            sb.append(count++).append(". ");
            sb.append("【").append(news.getSource()).append("】");
            sb.append(news.getTitle()).append("\n");
            if (news.getSummary() != null) {
                sb.append("   摘要：").append(news.getSummary()).append("\n");
            }
            if (news.getSentimentScore() != null) {
                sb.append("   情绪分数：").append(news.getSentimentScore()).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("请基于这些新闻：\n");
        sb.append("1. 分析新闻的整体情绪倾向（积极/消极/中性）\n");
        sb.append("2. 识别可能影响股价的重要事件或趋势\n");
        sb.append("3. 评估新闻对公司短期和中期走势的影响\n");
        sb.append("4. 给出基于新闻的投资建议（买入/卖出/观望）\n");
        sb.append("\n请提供专业、简洁的分析报告。");
        
        return sb.toString();
    }
    
    @Override
    public String getName() {
        return "新闻分析师";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.NEWS_ANALYST;
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.analyst.news.system 和 react.analyst.news.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.analyst.news";
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位资深的新闻分析师，擅长解读市场新闻和事件对股价的影响。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String symbol = state.getCompany();
        String dateStr = state.getDate() != null ? state.getDate().toString() : "N/A";
        return String.format(
                "目标：对 %s 在 %s 的相关新闻进行分析，评估整体情绪与事件影响，并给出投资建议（BUY/SELL/HOLD）。必要时调用工具。\n" +
                "请严格使用如下格式进行交互：\nThought: ...\nAction: <tool_name>\nAction Input: {json}\n在获得我返回的 Observation 后继续，直到给出 Final Answer。\n初始上下文：symbol=%s, date=%s",
                symbol, dateStr, symbol, dateStr
        );
    }
}
