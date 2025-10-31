package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.agents.analysts.MarketAnalyst;
import io.leavesfly.jtrade.agents.base.BaseRecAgent;
import io.leavesfly.jtrade.agents.base.AgentType;
import io.leavesfly.jtrade.config.AppConfig;
import io.leavesfly.jtrade.core.prompt.PromptManager;
import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
import io.leavesfly.jtrade.llm.client.LlmClient;

import java.time.LocalDate;

/**
 * PromptManager 模板化演示
 * 
 * 展示如何使用 PromptManager 管理 Agent 提示词模板
 * 
 * @author 山泽
 */
public class PromptManagerDemo {
    
    /**
     * 示例：继承 BaseRecAgent 并使用模板化提示
     */
    static class TemplatedMarketAnalyst extends BaseRecAgent {
        
        public TemplatedMarketAnalyst(LlmClient llmClient, 
                                     DataAggregator dataAggregator, 
                                     AppConfig appConfig,
                                     PromptManager promptManager) {
            super(llmClient, dataAggregator, appConfig, promptManager);
        }
        
        @Override
        public String getName() {
            return "市场分析师（模板版）";
        }
        
        @Override
        public AgentType getType() {
            return AgentType.MARKET_ANALYST;
        }
        
        /**
         * 覆盖此方法返回 PromptManager 中的提示键名
         * 返回 "react.analyst.market" 将加载：
         * - react.analyst.market.system（系统提示）
         * - react.analyst.market.prompt（用户提示）
         */
        @Override
        protected String getPromptKey() {
            return "react.analyst.market";
        }
    }
    
    /**
     * 演示模板化 vs 硬编码提示的对比
     */
    public static void main(String[] args) {
        System.out.println("=== PromptManager 模板化提示演示 ===\n");
        
        // 1. 传统硬编码方式（BaseRecAgent 默认行为）
        System.out.println("【方式一】硬编码提示（BaseRecAgent 默认）：");
        System.out.println("- 系统提示：直接在 buildSystemPrompt() 方法中硬编码");
        System.out.println("- 用户提示：直接在 buildInitialUserPrompt() 方法中硬编码");
        System.out.println("- 优点：简单直接，无需配置文件");
        System.out.println("- 缺点：修改提示需要重新编译代码，不灵活\n");
        
        // 2. 模板化方式（通过 PromptManager）
        System.out.println("【方式二】模板化提示（PromptManager）：");
        System.out.println("- 系统提示：从 agent-prompts.properties 加载");
        System.out.println("- 用户提示：从 agent-prompts.properties 加载");
        System.out.println("- 优点：修改提示只需编辑配置文件，无需重新编译");
        System.out.println("- 缺点：需要维护配置文件\n");
        
        // 3. 可用的 ReAct 提示模板键名
        System.out.println("【可用的 ReAct 提示模板键名】：");
        System.out.println("react.common                    - 通用 ReAct 提示");
        System.out.println("react.analyst.market            - 市场分析师");
        System.out.println("react.analyst.fundamentals      - 基本面分析师");
        System.out.println("react.analyst.news              - 新闻分析师");
        System.out.println("react.analyst.social            - 社交媒体分析师");
        System.out.println("react.researcher.bull           - 多头研究员");
        System.out.println("react.researcher.bear           - 空头研究员");
        System.out.println("react.trader                    - 交易员");
        System.out.println("react.manager.risk              - 风险管理器");
        System.out.println("react.debator.aggressive        - 激进辩论者");
        System.out.println("react.debator.conservative      - 保守辩论者");
        System.out.println("react.debator.neutral           - 中立辩论者\n");
        
        // 4. 使用示例
        System.out.println("【使用示例】：");
        System.out.println("```java");
        System.out.println("// 创建带 PromptManager 的 Agent");
        System.out.println("PromptManager promptManager = new PromptManager();");
        System.out.println("TemplatedMarketAnalyst analyst = new TemplatedMarketAnalyst(");
        System.out.println("    llmClient, dataAggregator, appConfig, promptManager");
        System.out.println(");");
        System.out.println("");
        System.out.println("// Agent 执行时会自动从配置文件加载提示");
        System.out.println("AgentState result = analyst.execute(state);");
        System.out.println("```\n");
        
        // 5. 提示变量说明
        System.out.println("【提示模板支持的变量】：");
        System.out.println("{symbol}                - 股票代码");
        System.out.println("{date}                  - 交易日期");
        System.out.println("{analystReports}        - 分析师报告汇总");
        System.out.println("{researcherViewpoints}  - 研究员观点汇总");
        System.out.println("{managerDecision}       - 研究经理决策");
        System.out.println("{tradingPlan}           - 交易计划");
        System.out.println("{riskDebateHistory}     - 风险辩论历史");
        System.out.println("{tools}                 - 可用工具列表（自动生成）\n");
        
        // 6. 配置文件位置
        System.out.println("【配置文件位置】：");
        System.out.println("src/main/resources/prompts/agent-prompts.properties\n");
        
        // 7. 优化效果总结
        System.out.println("【优化效果总结】：");
        System.out.println("✓ 提示词模板化管理，便于调优和版本控制");
        System.out.println("✓ 支持变量替换，动态注入上下文信息");
        System.out.println("✓ 统一的工具列表生成机制");
        System.out.println("✓ 降级机制：如果 PromptManager 为 null，自动使用硬编码提示");
        System.out.println("✓ 各角色可通过覆盖 getPromptKey() 使用专属模板");
        System.out.println("✓ 教育价值：清晰展示了配置管理最佳实践\n");
        
        System.out.println("=== 演示完成 ===");
    }
}
