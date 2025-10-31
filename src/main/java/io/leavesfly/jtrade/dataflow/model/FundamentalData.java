package io.leavesfly.jtrade.dataflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 基本面数据模型
 * 
 * @author 山泽
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundamentalData {
    
    /**
     * 股票代码
     */
    private String symbol;
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 市值
     */
    private BigDecimal marketCap;
    
    /**
     * 市盈率 (P/E)
     */
    private BigDecimal peRatio;
    
    /**
     * 市净率 (P/B)
     */
    private BigDecimal pbRatio;
    
    /**
     * 股息收益率
     */
    private BigDecimal dividendYield;
    
    /**
     * 每股收益 (EPS)
     */
    private BigDecimal eps;
    
    /**
     * 净资产收益率 (ROE)
     */
    private BigDecimal roe;
    
    /**
     * 资产负债率
     */
    private BigDecimal debtToEquity;
    
    /**
     * 毛利率
     */
    private BigDecimal grossMargin;
    
    /**
     * 行业
     */
    private String industry;
    
    /**
     * 板块
     */
    private String sector;
}
