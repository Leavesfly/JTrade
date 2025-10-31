package io.leavesfly.jtrade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM配置类
 * 
 * 支持多种LLM提供商：
 * - OpenAI (gpt-4o-mini, o1-mini)
 * - 通义千问 (qwen-plus, qwen-turbo)
 * - DeepSeek (deepseek-chat)
 * - Ollama (本地模型)
 * 
 * @author 山泽
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jtrade.llm")
public class LlmConfig {
    
    /**
     * LLM提供商选择: openai, qwen, deepseek, ollama
     */
    private String provider = "qwen";
    
    /**
     * 深度思考模型（用于复杂决策）
     */
    private String deepThinkModel;
    
    /**
     * 快速思考模型（用于快速分析）
     */
    private String quickThinkModel;
    
    /**
     * 通用配置
     */
    private int timeout = 60000;  // 超时时间（毫秒）
    private int maxRetries = 3;   // 最大重试次数
    
    /**
     * OpenAI配置
     */
    private ProviderConfig openai = new ProviderConfig();
    
    /**
     * 通义千问配置
     */
    private ProviderConfig qwen = new ProviderConfig();
    
    /**
     * DeepSeek配置
     */
    private ProviderConfig deepseek = new ProviderConfig();
    
    /**
     * Ollama配置
     */
    private ProviderConfig ollama = new ProviderConfig();
    
    /**
     * 提供商配置
     */
    @Data
    public static class ProviderConfig {
        private String apiKey;
        private String baseUrl;
        private String deepModel;
        private String quickModel;
    }
    
    /**
     * 根据当前提供商获取配置
     */
    public ProviderConfig getCurrentProviderConfig() {
        return switch (provider.toLowerCase()) {
            case "openai" -> openai;
            case "qwen" -> qwen;
            case "deepseek" -> deepseek;
            case "ollama" -> ollama;
            default -> throw new IllegalArgumentException("Unknown LLM provider: " + provider);
        };
    }
    
    /**
     * 获取深度思考模型名称
     */
    public String getDeepThinkModelName() {
        if (deepThinkModel != null && !deepThinkModel.isEmpty()) {
            return deepThinkModel;
        }
        return getCurrentProviderConfig().getDeepModel();
    }
    
    /**
     * 获取快速思考模型名称
     */
    public String getQuickThinkModelName() {
        if (quickThinkModel != null && !quickThinkModel.isEmpty()) {
            return quickThinkModel;
        }
        return getCurrentProviderConfig().getQuickModel();
    }
}
