package io.leavesfly.jtrade.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM消息模型
 * 
 * @author 山泽
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmMessage {
    
    /**
     * 消息角色: system, user, assistant, tool
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 创建系统消息
     */
    public static LlmMessage system(String content) {
        return LlmMessage.builder()
                .role("system")
                .content(content)
                .build();
    }
    
    /**
     * 创建用户消息
     */
    public static LlmMessage user(String content) {
        return LlmMessage.builder()
                .role("user")
                .content(content)
                .build();
    }
    
    /**
     * 创建助手消息
     */
    public static LlmMessage assistant(String content) {
        return LlmMessage.builder()
                .role("assistant")
                .content(content)
                .build();
    }
    
    /**
     * 创建工具消息
     */
    public static LlmMessage tool(String content) {
        return LlmMessage.builder()
                .role("tool")
                .content(content)
                .build();
    }
}
