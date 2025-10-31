package io.leavesfly.jtrade.agents.managers;

import io.leavesfly.jtrade.agents.base.BaseRecAgent;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.agents.base.AgentType;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.llm.client.LlmClient;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 研究经理
 * 
 * 综合多空双方观点，做出最终投资决策
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class ResearchManager extends BaseRecAgent {
    
    // Deleted: moved to BaseRecAgent
    // Deleted: moved to BaseRecAgent
    
    public ResearchManager(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }
    
    @Override
    public AgentState execute(AgentState state) {
        ReactResult result = performReact(state);
        AgentState updated = state.toBuilder()
                .researchManagerDecision(result.finalAnswer)
                .build();
        return updated.putMetadata("research_manager_trace", result.trace);
    }
    
    @Override
    protected String buildSystemPrompt() {
        String base = super.buildSystemPrompt();
        return "你是一位经验丰富的研究经理，负责综合多空双方的观点做出最终投资决策。\n" + base;
    }
    
    @Override
    protected String buildInitialUserPrompt(AgentState state) {
        String symbol = state.getCompany();
        String allViewpoints = String.join("\n\n", state.getResearcherViewpoints());
        return String.format(
                "请综合以下研究员观点，客观权衡并给出最终投资决策（BUY/SELL/HOLD），在最终答案中明确建议与依据。\n股票代码：%s\n观点汇总：\n%s",
                symbol, allViewpoints
        );
    }
    
    @Override
    public String getName() {
        return "研究经理";
    }
    
    /**
     * 使用 PromptManager 中的模板化提示
     * 注意：ResearchManager 当前使用传统格式，非 ReAct 模式
     * 对应模板：manager.research.system 和 manager.research.prompt
     */
    @Override
    protected String getPromptKey() {
        return "manager.research";
    }
    
    @Override
    public AgentType getType() {
        return AgentType.RESEARCH_MANAGER;
    }
}
