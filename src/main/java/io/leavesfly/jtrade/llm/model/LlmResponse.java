package io.leavesfly.jtrade.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM响应模型
 * 
 * @author 山泽
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    
    /**
     * 响应内容
     */
    private String content;
    
    /**
     * 响应ID
     */
    private String id;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * Token使用统计
     */
    private TokenUsage usage;
    
    /**
     * 完成原因
     */
    private String finishReason;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
    }
}
