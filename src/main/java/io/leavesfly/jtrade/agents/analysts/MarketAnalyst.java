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
 * 市场分析师
 * 
 * 负责分析技术指标和市场趋势
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class MarketAnalyst extends BaseRecAgent {
    
    // Deleted: moved to BaseRecAgent
    // Deleted: moved to BaseRecAgent
    // Deleted: moved to BaseRecAgent
    
    public MarketAnalyst(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        return super.execute(state);
    }
    
    private String buildPrompt(String symbol, Map<String, Double> indicators) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下股票的技术指标：\n\n");
        sb.append("股票代码：").append(symbol).append("\n\n");
        sb.append("技术指标：\n");
        
        indicators.forEach((key, value) -> 
            sb.append("- ").append(key).append(": ").append(value).append("\n")
        );
        
        sb.append("\n请基于这些技术指标：\n");
        sb.append("1. 分析当前市场趋势（上涨/下跌/横盘）\n");
        sb.append("2. 评估技术面强弱\n");
        sb.append("3. 识别关键支撑位和阻力位\n");
        sb.append("4. 给出技术面建议（买入/卖出/观望）\n");
        sb.append("\n请提供专业、简洁的分析报告。");
        
        return sb.toString();
    }
    
    @Override
    public String getName() {
        return "市场分析师";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.MARKET_ANALYST;
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.analyst.market.system 和 react.analyst.market.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.analyst.market";
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位资深的技术分析师，擅长分析市场趋势和技术指标。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String symbol = state.getCompany();
        String dateStr = state.getDate() != null ? state.getDate().toString() : "N/A";
        return String.format(
                "目标：对 %s 在 %s 的技术面进行分析，并给出趋势判断、技术强弱、支撑/阻力位与交易建议（BUY/SELL/HOLD）。必要时调用工具。\n" +
                "请严格使用如下格式进行交互：\nThought: ...\nAction: <tool_name>\nAction Input: {json}\n在获得我返回的 Observation 后继续，直到给出 Final Answer。\n初始上下文：symbol=%s, date=%s",
                symbol, dateStr, symbol, dateStr
        );
    }
}
