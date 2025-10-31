package io.leavesfly.jtrade.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置
 * 
 * @author 山泽
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfig {
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 温度参数 (0.0-2.0)
     */
    @Builder.Default
    private double temperature = 0.7;
    
    /**
     * 最大生成Token数
     */
    @Builder.Default
    private int maxTokens = 2000;
    
    /**
     * Top P采样参数
     */
    @Builder.Default
    private double topP = 0.9;
    
    /**
     * 频率惩罚
     */
    @Builder.Default
    private double frequencyPenalty = 0.0;
    
    /**
     * 存在惩罚
     */
    @Builder.Default
    private double presencePenalty = 0.0;
}
