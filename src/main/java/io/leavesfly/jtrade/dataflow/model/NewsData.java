package io.leavesfly.jtrade.dataflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新闻数据模型
 * 
 * @author 山泽
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsData {
    
    /**
     * 新闻标题
     */
    private String title;
    
    /**
     * 新闻来源
     */
    private String source;
    
    /**
     * 新闻URL
     */
    private String url;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;
    
    /**
     * 新闻摘要
     */
    private String summary;
    
    /**
     * 情绪分数（-1到1）
     */
    private Double sentimentScore;
}
