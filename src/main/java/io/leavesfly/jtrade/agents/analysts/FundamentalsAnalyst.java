package io.leavesfly.jtrade.agents.analysts;

import io.leavesfly.jtrade.agents.base.BaseRecAgent;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.agents.base.AgentType;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.dataflow.model.FundamentalData;
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
 * 基本面分析师
 * 
 * 负责分析公司财务数据和基本面指标
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class FundamentalsAnalyst extends BaseRecAgent {

    public FundamentalsAnalyst(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        return super.execute(state);
    }
    
    private String buildPrompt(String symbol, FundamentalData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下公司的基本面数据：\n\n");
        sb.append("公司名称：").append(data.getCompanyName()).append("\n");
        sb.append("股票代码：").append(symbol).append("\n\n");
        sb.append("财务指标：\n");
        
        if (data.getMarketCap() != null) {
            sb.append("- 市值：").append(data.getMarketCap()).append("\n");
        }
        if (data.getPeRatio() != null) {
            sb.append("- 市盈率 (P/E)：").append(data.getPeRatio()).append("\n");
        }
        if (data.getPbRatio() != null) {
            sb.append("- 市净率 (P/B)：").append(data.getPbRatio()).append("\n");
        }
        if (data.getEps() != null) {
            sb.append("- 每股收益 (EPS)：").append(data.getEps()).append("\n");
        }
        if (data.getDividendYield() != null) {
            sb.append("- 股息收益率：").append(data.getDividendYield()).append("%\n");
        }
        
        sb.append("\n请基于这些基本面数据：\n");
        sb.append("1. 评估公司的估值水平（高估/合理/低估）\n");
        sb.append("2. 分析公司的盈利能力和财务健康状况\n");
        sb.append("3. 识别潜在的投资风险和机会\n");
        sb.append("4. 给出基本面投资建议（买入/卖出/观望）\n");
        sb.append("\n请提供专业、简洁的分析报告。");
        
        return sb.toString();
    }
    
    @Override
    public String getName() {
        return "基本面分析师";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.FUNDAMENTALS_ANALYST;
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 对应模板：react.analyst.fundamentals.system 和 react.analyst.fundamentals.prompt
     */
    @Override
    protected String getPromptKey() {
        return "react.analyst.fundamentals";
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位资深的基本面分析师，擅长分析公司财务数据和估值。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String symbol = state.getCompany();
        String dateStr = state.getDate() != null ? state.getDate().toString() : "N/A";
        return String.format(
                "目标：对 %s 在 %s 的基本面进行分析，并给出估值判断、盈利能力、风险与投资建议（BUY/SELL/HOLD）。必要时调用工具。\n" +
                "请严格使用如下格式进行交互：\nThought: ...\nAction: <tool_name>\nAction Input: {json}\n在获得我返回的 Observation 后继续，直到给出 Final Answer。\n初始上下文：symbol=%s, date=%s",
                symbol, dateStr, symbol, dateStr
        );
    }
}
