package io.leavesfly.jtrade.dataflow.provider;

import io.leavesfly.jtrade.dataflow.model.MarketData;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据提供者接口
 * 
 * 统一的数据获取接口规范
 * 
 * @author 山泽
 */
public interface DataProvider {
    
    /**
     * 获取市场数据
     * 
     * @param symbol 股票代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 市场数据列表
     */
    List<MarketData> fetchMarketData(String symbol, LocalDate startDate, LocalDate endDate);
    
    /**
     * 判断是否支持某个数据源
     * 
     * @return 是否启用
     */
    boolean isEnabled();
    
    /**
     * 获取提供者名称
     * 
     * @return 提供者名称
     */
    String getProviderName();
}
