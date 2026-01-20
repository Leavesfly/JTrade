package io.leavesfly.jtrade.core.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
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
     * 获取 Prompt 模板（通用方法）
     * 
     * @param key 智能体标识，如 "analyst.market" 或 "react.trader"
     * @return 包含系统提示和用户提示的模板
     */
    public PromptTemplate getTemplate(String key) {
        return new PromptTemplate(
            getSystemPrompt(key),
            getUserPrompt(key)
        );
    }
    
    // ========== 兼容旧版本的便捷方法（建议使用 getTemplate 代替） ==========
    
    public PromptTemplate getMarketAnalystPrompt() { return getTemplate("analyst.market"); }
    public PromptTemplate getFundamentalsAnalystPrompt() { return getTemplate("analyst.fundamentals"); }
    public PromptTemplate getNewsAnalystPrompt() { return getTemplate("analyst.news"); }
    public PromptTemplate getSocialMediaAnalystPrompt() { return getTemplate("analyst.social"); }
    public PromptTemplate getBullResearcherPrompt() { return getTemplate("researcher.bull"); }
    public PromptTemplate getBearResearcherPrompt() { return getTemplate("researcher.bear"); }
    public PromptTemplate getResearchManagerPrompt() { return getTemplate("manager.research"); }
    public PromptTemplate getTraderPrompt() { return getTemplate("trader"); }
    public PromptTemplate getRiskManagerPrompt() { return getTemplate("manager.risk"); }
    public PromptTemplate getAnalysisReflectionPrompt() { return getTemplate("reflection.analysis"); }
    public PromptTemplate getDecisionReflectionPrompt() { return getTemplate("reflection.decision"); }
    public PromptTemplate getComprehensiveReflectionPrompt() { return getTemplate("reflection.comprehensive"); }
    
    // ========== ReAct 工具智能体提示 ==========
    
    public PromptTemplate getReactCommonPrompt() { return getTemplate("react.common"); }
    public PromptTemplate getReactMarketAnalystPrompt() { return getTemplate("react.analyst.market"); }
    public PromptTemplate getReactFundamentalsAnalystPrompt() { return getTemplate("react.analyst.fundamentals"); }
    public PromptTemplate getReactNewsAnalystPrompt() { return getTemplate("react.analyst.news"); }
    public PromptTemplate getReactSocialMediaAnalystPrompt() { return getTemplate("react.analyst.social"); }
    public PromptTemplate getReactBullResearcherPrompt() { return getTemplate("react.researcher.bull"); }
    public PromptTemplate getReactBearResearcherPrompt() { return getTemplate("react.researcher.bear"); }
    public PromptTemplate getReactTraderPrompt() { return getTemplate("react.trader"); }
    public PromptTemplate getReactRiskManagerPrompt() { return getTemplate("react.manager.risk"); }
    public PromptTemplate getReactAggressiveDebatorPrompt() { return getTemplate("react.debator.aggressive"); }
    public PromptTemplate getReactConservativeDebatorPrompt() { return getTemplate("react.debator.conservative"); }
    public PromptTemplate getReactNeutralDebatorPrompt() { return getTemplate("react.debator.neutral"); }
    
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
