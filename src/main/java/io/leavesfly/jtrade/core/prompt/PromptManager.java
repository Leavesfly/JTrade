package io.leavesfly.jtrade.core.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Prompt 管理器
 * 
 * 从配置文件加载和管理所有智能体的 Prompt 模板
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class PromptManager {
    
    private final Properties prompts = new Properties();
    private final Map<String, String> cache = new HashMap<>();
    
    public PromptManager() {
        loadPrompts();
    }
    
    /**
     * 加载 Prompt 配置文件
     */
    private void loadPrompts() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/agent-prompts.properties");
            try (InputStream is = resource.getInputStream()) {
                prompts.load(is);
                log.info("成功加载 {} 个 Prompt 模板", prompts.size());
            }
        } catch (IOException e) {
            log.error("加载 Prompt 配置文件失败", e);
        }
    }
    
    /**
     * 获取 Prompt 模板
     */
    public String getPrompt(String key) {
        return prompts.getProperty(key, "");
    }
    
    /**
     * 获取系统提示
     */
    public String getSystemPrompt(String agentType) {
        return getPrompt(agentType + ".system");
    }
    
    /**
     * 获取用户提示
     */
    public String getUserPrompt(String agentType) {
        return getPrompt(agentType + ".prompt");
    }
    
    /**
     * 构建完整的 Prompt（替换变量）
     */
    public String buildPrompt(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
    
    /**
     * 获取市场分析师提示
     */
    public PromptTemplate getMarketAnalystPrompt() {
        return new PromptTemplate(
            getSystemPrompt("analyst.market"),
            getUserPrompt("analyst.market")
        );
    }
    
    /**
     * 获取基本面分析师提示
     */
    public PromptTemplate getFundamentalsAnalystPrompt() {
        return new PromptTemplate(
            getSystemPrompt("analyst.fundamentals"),
            getUserPrompt("analyst.fundamentals")
        );
    }
    
    /**
     * 获取新闻分析师提示
     */
    public PromptTemplate getNewsAnalystPrompt() {
        return new PromptTemplate(
            getSystemPrompt("analyst.news"),
            getUserPrompt("analyst.news")
        );
    }
    
    /**
     * 获取社交媒体分析师提示
     */
    public PromptTemplate getSocialMediaAnalystPrompt() {
        return new PromptTemplate(
            getSystemPrompt("analyst.social"),
            getUserPrompt("analyst.social")
        );
    }
    
    /**
     * 获取多头研究员提示
     */
    public PromptTemplate getBullResearcherPrompt() {
        return new PromptTemplate(
            getSystemPrompt("researcher.bull"),
            getUserPrompt("researcher.bull")
        );
    }
    
    /**
     * 获取空头研究员提示
     */
    public PromptTemplate getBearResearcherPrompt() {
        return new PromptTemplate(
            getSystemPrompt("researcher.bear"),
            getUserPrompt("researcher.bear")
        );
    }
    
    /**
     * 获取研究经理提示
     */
    public PromptTemplate getResearchManagerPrompt() {
        return new PromptTemplate(
            getSystemPrompt("manager.research"),
            getUserPrompt("manager.research")
        );
    }
    
    /**
     * 获取交易员提示
     */
    public PromptTemplate getTraderPrompt() {
        return new PromptTemplate(
            getSystemPrompt("trader"),
            getUserPrompt("trader")
        );
    }
    
    /**
     * 获取风险管理器提示
     */
    public PromptTemplate getRiskManagerPrompt() {
        return new PromptTemplate(
            getSystemPrompt("manager.risk"),
            getUserPrompt("manager.risk")
        );
    }
    
    /**
     * 获取分析反思提示
     */
    public PromptTemplate getAnalysisReflectionPrompt() {
        return new PromptTemplate(
            getSystemPrompt("reflection.analysis"),
            getUserPrompt("reflection.analysis")
        );
    }
    
    /**
     * 获取决策反思提示
     */
    public PromptTemplate getDecisionReflectionPrompt() {
        return new PromptTemplate(
            getSystemPrompt("reflection.decision"),
            getUserPrompt("reflection.decision")
        );
    }
    
    /**
     * 获取综合反思提示
     */
    public PromptTemplate getComprehensiveReflectionPrompt() {
        return new PromptTemplate(
            getSystemPrompt("reflection.comprehensive"),
            getUserPrompt("reflection.comprehensive")
        );
    }
    
    // ========== ReAct 工具智能体提示 ==========
    
    /**
     * 获取通用 ReAct 提示
     */
    public PromptTemplate getReactCommonPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.common"),
            getUserPrompt("react.common")
        );
    }
    
    /**
     * 获取市场分析师 ReAct 提示
     */
    public PromptTemplate getReactMarketAnalystPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.analyst.market"),
            getUserPrompt("react.analyst.market")
        );
    }
    
    /**
     * 获取基本面分析师 ReAct 提示
     */
    public PromptTemplate getReactFundamentalsAnalystPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.analyst.fundamentals"),
            getUserPrompt("react.analyst.fundamentals")
        );
    }
    
    /**
     * 获取新闻分析师 ReAct 提示
     */
    public PromptTemplate getReactNewsAnalystPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.analyst.news"),
            getUserPrompt("react.analyst.news")
        );
    }
    
    /**
     * 获取社交媒体分析师 ReAct 提示
     */
    public PromptTemplate getReactSocialMediaAnalystPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.analyst.social"),
            getUserPrompt("react.analyst.social")
        );
    }
    
    /**
     * 获取多头研究员 ReAct 提示
     */
    public PromptTemplate getReactBullResearcherPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.researcher.bull"),
            getUserPrompt("react.researcher.bull")
        );
    }
    
    /**
     * 获取空头研究员 ReAct 提示
     */
    public PromptTemplate getReactBearResearcherPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.researcher.bear"),
            getUserPrompt("react.researcher.bear")
        );
    }
    
    /**
     * 获取交易员 ReAct 提示
     */
    public PromptTemplate getReactTraderPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.trader"),
            getUserPrompt("react.trader")
        );
    }
    
    /**
     * 获取风险管理器 ReAct 提示
     */
    public PromptTemplate getReactRiskManagerPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.manager.risk"),
            getUserPrompt("react.manager.risk")
        );
    }
    
    /**
     * 获取激进辩论者 ReAct 提示
     */
    public PromptTemplate getReactAggressiveDebatorPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.debator.aggressive"),
            getUserPrompt("react.debator.aggressive")
        );
    }
    
    /**
     * 获取保守辩论者 ReAct 提示
     */
    public PromptTemplate getReactConservativeDebatorPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.debator.conservative"),
            getUserPrompt("react.debator.conservative")
        );
    }
    
    /**
     * 获取中立辩论者 ReAct 提示
     */
    public PromptTemplate getReactNeutralDebatorPrompt() {
        return new PromptTemplate(
            getSystemPrompt("react.debator.neutral"),
            getUserPrompt("react.debator.neutral")
        );
    }
    
    /**
     * Prompt 模板类
     */
    public static class PromptTemplate {
        private final String systemPrompt;
        private final String userPrompt;
        
        public PromptTemplate(String systemPrompt, String userPrompt) {
            this.systemPrompt = systemPrompt;
            this.userPrompt = userPrompt;
        }
        
        public String getSystemPrompt() {
            return systemPrompt;
        }
        
        public String getUserPrompt() {
            return userPrompt;
        }
        
        /**
         * 构建用户提示（替换变量）
         */
        public String buildUserPrompt(Map<String, String> variables) {
            String result = userPrompt;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            return result;
        }
    }
}
