package io.leavesfly.jtrade.agents.analysts;

import io.leavesfly.jtrade.agents.base.BaseRecAgent;
import io.leavesfly.jtrade.agents.base.AgentType;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
import io.leavesfly.jtrade.llm.client.LlmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 综合分析师 (ComprehensiveAnalyst)
 * 
 * 作为一个全能型研究员，通过继承 BaseRecAgent 实现 ReAct 循环，
 * 能够综合利用各类工具进行深度投资价值研究。
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class ComprehensiveAnalyst extends BaseRecAgent {

    public ComprehensiveAnalyst(LlmClient llmClient, DataAggregator dataAggregator, AppConfig appConfig) {
        super(llmClient, dataAggregator, appConfig);
    }

    @Override
    public String getName() {
        return "综合分析师";
    }

    @Override
    public AgentType getType() {
        return AgentType.COMPREHENSIVE_ANALYST;
    }

    @Override
    protected String getPromptKey() {
        // 使用通用的 ReAct 模板
        return "react.common";
    }
}
