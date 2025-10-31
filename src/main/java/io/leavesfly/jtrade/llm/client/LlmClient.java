package io.leavesfly.jtrade.llm.client;

import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;

import java.util.List;

/**
 * LLM客户端接口
 * 
 * 统一的LLM调用接口，支持多种提供商
 * 
 * @author 山泽
 */
public interface LlmClient {
    
    /**
     * 发起对话
     * 
     * @param messages 消息列表
     * @param config 模型配置
     * @return LLM响应
     */
    LlmResponse chat(List<LlmMessage> messages, ModelConfig config);
    
    /**
     * 流式对话（可选）
     * 
     * @param messages 消息列表
     * @param config 模型配置
     * @param callback 流式回调
     */
    default void streamChat(List<LlmMessage> messages, ModelConfig config, StreamCallback callback) {
        throw new UnsupportedOperationException("Stream chat not supported");
    }
    
    /**
     * 文本向量化（可选）
     * 
     * @param text 文本内容
     * @return 向量数组
     */
    default double[] embedText(String text) {
        throw new UnsupportedOperationException("Embedding not supported");
    }
    
    /**
     * 流式回调接口
     */
    @FunctionalInterface
    interface StreamCallback {
        void onChunk(String chunk);
    }
}
