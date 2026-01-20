package io.leavesfly.jtrade;

import io.leavesfly.jtrade.config.LlmConfig;
import io.leavesfly.jtrade.llm.client.LlmClient;
import io.leavesfly.jtrade.llm.client.SimpleLlmClient;
import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * SimpleLlmClient测试
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootTest(classes = JTradeApplication.class)
public class SimpleLlmClientTest {
    
    @Autowired
    private LlmClient llmClient;
    
    @Autowired
    private LlmConfig llmConfig;
    
    @Test
    public void testChat() {
        // 准备测试消息
        List<LlmMessage> messages = List.of(
                LlmMessage.system("你是一个专业的金融分析师。"),
                LlmMessage.user("请简要分析一下苹果公司(AAPL)的投资价值。")
        );
        
        // 配置模型
        ModelConfig config = ModelConfig.builder()
                .model(llmConfig.getQuickThinkModelName())
                .temperature(0.7)
                .maxTokens(500)
                .build();
        
        // 调用LLM
        log.info("正在调用LLM，提供商: {}, 模型: {}", 
                llmConfig.getProvider(), config.getModel());
        
        LlmResponse response = llmClient.chat(messages, config);
        
        // 输出结果
        log.info("LLM响应: {}", response.getContent());
        log.info("Token使用: {}", response.getUsage());
        
        // 验证响应不为空
        assert response.getContent() != null && !response.getContent().isEmpty();
    }
}
