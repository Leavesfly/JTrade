package io.leavesfly.jtrade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用通用配置类
 * 
 * 管理项目目录、结果输出目录、数据目录等通用配置
 * 
 * @author 山泽
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jtrade")
public class AppConfig {
    
    /**
     * 项目根目录
     */
    private String projectDir = "./JTrade";
    
    /**
     * 结果输出目录
     */
    private String resultsDir = "./results";
    
    /**
     * 数据存储目录
     */
    private String dataDir = "./data";
    
    /**
     * 数据缓存目录
     */
    private String dataCacheDir = "./data/cache";
    
    /**
     * 辩论配置
     */
    private DebateConfig debate = new DebateConfig();
    
    /**
     * 风险辩论配置
     */
    private RiskDebateConfig riskDebate = new RiskDebateConfig();
    
    /**
     * 工作流配置
     */
    private WorkflowConfig workflow = new WorkflowConfig();
    
    /**
     * 数据源配置
     */
    private DataSourceConfig dataSource = new DataSourceConfig();
    
    @Data
    public static class DebateConfig {
        /**
         * 最大辩论轮数
         */
        private int maxRounds = 1;
    }
    
    @Data
    public static class RiskDebateConfig {
        /**
         * 最大风险辩论轮数
         */
        private int maxRounds = 1;
    }
    
    @Data
    public static class WorkflowConfig {
        /**
         * 最大递归限制
         */
        private int maxRecursionLimit = 100;
    }
    
    @Data
    public static class DataSourceConfig {
        /**
         * 是否使用在线工具
         */
        private boolean onlineTools = true;
    }
}
