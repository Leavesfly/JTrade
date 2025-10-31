package io.leavesfly.jtrade.llm.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.leavesfly.jtrade.config.LlmConfig;
import io.leavesfly.jtrade.llm.exception.AuthenticationException;
import io.leavesfly.jtrade.llm.exception.LlmException;
import io.leavesfly.jtrade.llm.exception.RateLimitException;
import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 简化的LLM客户端实现
 * 
 * 支持多种OpenAI兼容的LLM提供商：
 * - OpenAI
 * - 通义千问
 * - DeepSeek
 * - Ollama
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class SimpleLlmClient implements LlmClient {
    
    private final LlmConfig llmConfig;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public SimpleLlmClient(LlmConfig llmConfig) {
        this.llmConfig = llmConfig;
        this.objectMapper = new ObjectMapper();
        
        // 初始化HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(llmConfig.getTimeout()))
                .readTimeout(Duration.ofMillis(llmConfig.getTimeout()))
                .writeTimeout(Duration.ofMillis(llmConfig.getTimeout()))
                .build();
        
        log.info("SimpleLlmClient initialized with provider: {}", llmConfig.getProvider());
    }
    
    @Override
    public LlmResponse chat(List<LlmMessage> messages, ModelConfig config) {
        int retries = 0;
        long baseDelay = 1000; // 1秒基础延迟
        
        while (retries <= llmConfig.getMaxRetries()) {
            try {
                return doChat(messages, config);
            } catch (RateLimitException e) {
                retries++;
                if (retries > llmConfig.getMaxRetries()) {
                    throw e;
                }
                
                // 指数退避
                long delay = Math.min(baseDelay * (1L << (retries - 1)), 60000);
                log.warn("Rate limit exceeded, retrying in {}ms (attempt {}/{})", 
                        delay, retries, llmConfig.getMaxRetries());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LlmException("Interrupted during retry", ie);
                }
            } catch (IOException e) {
                retries++;
                if (retries > llmConfig.getMaxRetries()) {
                    throw new LlmException("Network error after " + retries + " retries", e);
                }
                
                long delay = Math.min(baseDelay * (1L << (retries - 1)), 60000);
                log.warn("Network error, retrying in {}ms (attempt {}/{}): {}", 
                        delay, retries, llmConfig.getMaxRetries(), e.getMessage());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LlmException("Interrupted during retry", ie);
                }
            }
        }
        
        throw new LlmException("Failed after " + llmConfig.getMaxRetries() + " retries");
    }
    
    /**
     * 执行实际的LLM调用
     */
    private LlmResponse doChat(List<LlmMessage> messages, ModelConfig config) throws IOException {
        LlmConfig.ProviderConfig providerConfig = llmConfig.getCurrentProviderConfig();
        
        // 构建请求JSON
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens());
        requestBody.put("top_p", config.getTopP());
        
        if (config.getFrequencyPenalty() != 0.0) {
            requestBody.put("frequency_penalty", config.getFrequencyPenalty());
        }
        if (config.getPresencePenalty() != 0.0) {
            requestBody.put("presence_penalty", config.getPresencePenalty());
        }
        
        // 添加消息
        ArrayNode messagesNode = requestBody.putArray("messages");
        for (LlmMessage message : messages) {
            ObjectNode messageNode = messagesNode.addObject();
            messageNode.put("role", message.getRole());
            messageNode.put("content", message.getContent());
        }
        
        // 构建HTTP请求
        Request.Builder requestBuilder = new Request.Builder()
                .url(providerConfig.getBaseUrl() + "/chat/completions")
                .post(RequestBody.create(
                        objectMapper.writeValueAsString(requestBody),
                        MediaType.get("application/json; charset=utf-8")
                ));
        
        // 添加认证头
        if (providerConfig.getApiKey() != null && !providerConfig.getApiKey().isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + providerConfig.getApiKey());
        }
        
        Request request = requestBuilder.build();
        
        log.debug("Sending LLM request to {} with model {}", providerConfig.getBaseUrl(), config.getModel());
        
        // 发送请求
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            // 处理HTTP错误
            if (!response.isSuccessful()) {
                handleHttpError(response.code(), responseBody);
            }
            
            // 解析响应
            return parseResponse(responseBody);
        }
    }
    
    /**
     * 处理HTTP错误
     */
    private void handleHttpError(int statusCode, String responseBody) {
        String errorMessage = "Unknown error";
        
        try {
            JsonNode errorNode = objectMapper.readTree(responseBody);
            if (errorNode.has("error")) {
                JsonNode error = errorNode.get("error");
                if (error.has("message")) {
                    errorMessage = error.get("message").asText();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse error response: {}", responseBody);
        }
        
        switch (statusCode) {
            case 401:
                throw new AuthenticationException("API Key错误: " + errorMessage);
            case 429:
                throw new RateLimitException("超出限流: " + errorMessage);
            case 500:
            case 502:
            case 503:
            case 504:
                throw new LlmException("服务器错误 (" + statusCode + "): " + errorMessage);
            default:
                throw new LlmException("HTTP错误 (" + statusCode + "): " + errorMessage);
        }
    }
    
    /**
     * 解析LLM响应
     */
    private LlmResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // 提取响应内容
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();
            
            String id = root.path("id").asText();
            String model = root.path("model").asText();
            String finishReason = root.path("choices")
                    .path(0)
                    .path("finish_reason")
                    .asText();
            
            // 提取Token使用统计
            LlmResponse.TokenUsage usage = null;
            if (root.has("usage")) {
                JsonNode usageNode = root.get("usage");
                usage = LlmResponse.TokenUsage.builder()
                        .promptTokens(usageNode.path("prompt_tokens").asInt())
                        .completionTokens(usageNode.path("completion_tokens").asInt())
                        .totalTokens(usageNode.path("total_tokens").asInt())
                        .build();
            }
            
            LlmResponse response = LlmResponse.builder()
                    .content(content)
                    .id(id)
                    .model(model)
                    .finishReason(finishReason)
                    .usage(usage)
                    .build();
            
            log.debug("LLM response received: {} tokens", 
                    usage != null ? usage.getTotalTokens() : 0);
            
            return response;
            
        } catch (Exception e) {
            throw new LlmException("Failed to parse LLM response: " + responseBody, e);
        }
    }
}
