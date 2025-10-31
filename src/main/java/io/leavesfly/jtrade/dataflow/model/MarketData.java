package io.leavesfly.jtrade.dataflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 市场数据模型
 * 
 * @author 山泽
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    
    /**
     * 股票代码
     */
    private String symbol;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 开盘价
     */
    private BigDecimal open;
    
    /**
     * 最高价
     */
    private BigDecimal high;
    
    /**
     * 最低价
     */
    private BigDecimal low;
    
    /**
     * 收盘价
     */
    private BigDecimal close;
    
    /**
     * 成交量
     */
    private Long volume;
    
    /**
     * 调整后收盘价
     */
    private BigDecimal adjustedClose;
}
