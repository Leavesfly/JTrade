package io.leavesfly.jtrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

/**
 * JTrade - Java版多智能体交易系统
 * 
 * 基于Spring Boot的多智能体金融交易研究框架
 * 支持多种LLM提供商：OpenAI、通义千问、DeepSeek、Ollama
 * 
 * @author 山泽
 * @since 2025-10-30
 */
@EnableCaching
@SpringBootApplication
@ConfigurationPropertiesScan
public class JTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(JTradeApplication.class, args);
    }
}
