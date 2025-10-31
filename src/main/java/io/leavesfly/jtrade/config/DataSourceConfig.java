package io.leavesfly.jtrade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 数据源配置类
 * 
 * 管理各类外部数据源的API配置
 * 
 * @author 山泽
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jtrade.datasource")
public class DataSourceConfig {
    
    /**
     * Finnhub配置
     */
    private ApiConfig finnhub = new ApiConfig();
    
    /**
     * Yahoo Finance配置
     */
    private ApiConfig yahooFinance = new ApiConfig();
    
    /**
     * Alpha Vantage配置
     */
    private ApiConfig alphaVantage = new ApiConfig();
    
    /**
     * Financial Modeling Prep配置
     */
    private ApiConfig fmp = new ApiConfig();
    
    /**
     * API配置
     */
    @Data
    public static class ApiConfig {
        private String apiKey;
        private String baseUrl;
        private boolean enabled = true;
    }
}
