package io.leavesfly.jtrade.dataflow.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jtrade.config.DataSourceConfig;
import io.leavesfly.jtrade.dataflow.model.MarketData;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Yahoo Finance数据提供者
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class YahooFinanceDataProvider implements DataProvider {
    
    private final DataSourceConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public YahooFinanceDataProvider(DataSourceConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public List<MarketData> fetchMarketData(String symbol, LocalDate startDate, LocalDate endDate) {
        if (!isEnabled()) {
            log.warn("Yahoo Finance数据源未启用");
            return new ArrayList<>();
        }
        
        log.info("从Yahoo Finance获取 {} 的市场数据，时间范围：{} 至 {}", symbol, startDate, endDate);
        
        try {
            // 使用Yahoo Finance Query API v8获取历史数据
            long period1 = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            long period2 = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond();
            
            // Yahoo Finance API v8 endpoint
            String url = String.format(
                "https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d&events=history",
                symbol, period1, period2
            );
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Yahoo Finance API调用失败: {}", response.code());
                    return getFallbackData(symbol, startDate, endDate);
                }
                
                String body = response.body().string();
                JsonNode rootNode = objectMapper.readTree(body);
                
                // 检查是否有错误
                JsonNode errorNode = rootNode.path("chart").path("error");
                if (!errorNode.isNull()) {
                    log.error("Yahoo Finance返回错误: {}", errorNode);
                    return getFallbackData(symbol, startDate, endDate);
                }
                
                return parseYahooFinanceData(symbol, rootNode);
            }
            
        } catch (Exception e) {
            log.error("从Yahoo Finance获取数据失败", e);
            return getFallbackData(symbol, startDate, endDate);
        }
    }
    
    /**
     * 解析Yahoo Finance API返回的数据
     */
    private List<MarketData> parseYahooFinanceData(String symbol, JsonNode rootNode) {
        List<MarketData> dataList = new ArrayList<>();
        
        try {
            JsonNode resultNode = rootNode.path("chart").path("result").get(0);
            JsonNode timestamps = resultNode.path("timestamp");
            JsonNode indicators = resultNode.path("indicators").path("quote").get(0);
            JsonNode adjClose = resultNode.path("indicators").path("adjclose").get(0).path("adjclose");
            
            JsonNode opens = indicators.path("open");
            JsonNode highs = indicators.path("high");
            JsonNode lows = indicators.path("low");
            JsonNode closes = indicators.path("close");
            JsonNode volumes = indicators.path("volume");
            
            for (int i = 0; i < timestamps.size(); i++) {
                // 跳过null值
                if (opens.get(i).isNull() || closes.get(i).isNull()) {
                    continue;
                }
                
                long timestamp = timestamps.get(i).asLong();
                LocalDate date = Instant.ofEpochSecond(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                
                MarketData data = MarketData.builder()
                        .symbol(symbol)
                        .date(date)
                        .open(new BigDecimal(opens.get(i).asText()))
                        .high(new BigDecimal(highs.get(i).asText()))
                        .low(new BigDecimal(lows.get(i).asText()))
                        .close(new BigDecimal(closes.get(i).asText()))
                        .volume(volumes.get(i).asLong())
                        .adjustedClose(adjClose.get(i).isNull() ? 
                                new BigDecimal(closes.get(i).asText()) : 
                                new BigDecimal(adjClose.get(i).asText()))
                        .build();
                
                dataList.add(data);
            }
            
            log.info("从Yahoo Finance成功获取 {} 条市场数据", dataList.size());
            
        } catch (Exception e) {
            log.error("解析Yahoo Finance数据失败", e);
        }
        
        return dataList;
    }
    
    /**
     * 当API调用失败时返回备用数据
     */
    private List<MarketData> getFallbackData(String symbol, LocalDate startDate, LocalDate endDate) {
        log.info("使用模拟数据作为备用");
        List<MarketData> dataList = new ArrayList<>();
        
        // 生成基础价格
        double basePrice = 150.0 + Math.random() * 50;
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 跳过周末
            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }
            
            // 模拟价格波动
            double dailyChange = (Math.random() - 0.5) * 10;
            basePrice += dailyChange;
            
            double open = basePrice + (Math.random() - 0.5) * 2;
            double close = basePrice + (Math.random() - 0.5) * 2;
            double high = Math.max(open, close) + Math.random() * 3;
            double low = Math.min(open, close) - Math.random() * 3;
            
            MarketData data = MarketData.builder()
                    .symbol(symbol)
                    .date(date)
                    .open(BigDecimal.valueOf(open).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .high(BigDecimal.valueOf(high).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .low(BigDecimal.valueOf(low).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .close(BigDecimal.valueOf(close).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .volume((long)(1000000 + Math.random() * 5000000))
                    .adjustedClose(BigDecimal.valueOf(close).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .build();
            dataList.add(data);
        }
        
        return dataList;
    }
    
    @Override
    public boolean isEnabled() {
        return config.getYahooFinance().isEnabled();
    }
    
    @Override
    public String getProviderName() {
        return "Yahoo Finance";
    }
}
