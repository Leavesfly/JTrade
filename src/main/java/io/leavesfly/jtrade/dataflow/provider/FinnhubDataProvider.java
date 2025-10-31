package io.leavesfly.jtrade.dataflow.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jtrade.config.DataSourceConfig;
import io.leavesfly.jtrade.dataflow.model.MarketData;
import io.leavesfly.jtrade.dataflow.model.NewsData;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Finnhub数据提供者
 * 
 * 集成Finnhub API获取真实市场数据
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class FinnhubDataProvider implements DataProvider {
    
    private final DataSourceConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public FinnhubDataProvider(DataSourceConfig config) {
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
            log.warn("Finnhub数据源未启用");
            return new ArrayList<>();
        }
        
        try {
            String apiKey = config.getFinnhub().getApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("Finnhub API密钥未配置");
                return new ArrayList<>();
            }
            
            long startTimestamp = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            long endTimestamp = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond();
            
            String url = String.format(
                "%s/stock/candle?symbol=%s&resolution=D&from=%d&to=%d&token=%s",
                config.getFinnhub().getBaseUrl(),
                symbol,
                startTimestamp,
                endTimestamp,
                apiKey
            );
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Finnhub API调用失败: {}", response.code());
                    return new ArrayList<>();
                }
                
                String body = response.body().string();
                JsonNode rootNode = objectMapper.readTree(body);
                
                if (rootNode.get("s").asText().equals("no_data")) {
                    log.warn("Finnhub未返回数据");
                    return new ArrayList<>();
                }
                
                return parseMarketData(symbol, rootNode);
            }
            
        } catch (Exception e) {
            log.error("获取Finnhub数据失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取公司新闻
     */
    public List<NewsData> fetchNews(String symbol, LocalDate fromDate, LocalDate toDate) {
        if (!isEnabled()) {
            return new ArrayList<>();
        }
        
        try {
            String apiKey = config.getFinnhub().getApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                return new ArrayList<>();
            }
            
            String url = String.format(
                "%s/company-news?symbol=%s&from=%s&to=%s&token=%s",
                config.getFinnhub().getBaseUrl(),
                symbol,
                fromDate.toString(),
                toDate.toString(),
                apiKey
            );
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Finnhub新闻API调用失败: {}", response.code());
                    return new ArrayList<>();
                }
                
                String body = response.body().string();
                JsonNode arrayNode = objectMapper.readTree(body);
                
                return parseNewsData(arrayNode);
            }
            
        } catch (Exception e) {
            log.error("获取Finnhub新闻失败", e);
            return new ArrayList<>();
        }
    }
    
    private List<MarketData> parseMarketData(String symbol, JsonNode rootNode) {
        List<MarketData> dataList = new ArrayList<>();
        
        JsonNode timestamps = rootNode.get("t");
        JsonNode opens = rootNode.get("o");
        JsonNode highs = rootNode.get("h");
        JsonNode lows = rootNode.get("l");
        JsonNode closes = rootNode.get("c");
        JsonNode volumes = rootNode.get("v");
        
        if (timestamps == null || !timestamps.isArray()) {
            return dataList;
        }
        
        for (int i = 0; i < timestamps.size(); i++) {
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
                    .adjustedClose(new BigDecimal(closes.get(i).asText()))
                    .build();
            
            dataList.add(data);
        }
        
        log.info("从Finnhub获取到 {} 条市场数据", dataList.size());
        return dataList;
    }
    
    private List<NewsData> parseNewsData(JsonNode arrayNode) {
        List<NewsData> newsList = new ArrayList<>();
        
        if (!arrayNode.isArray()) {
            return newsList;
        }
        
        for (JsonNode newsNode : arrayNode) {
            NewsData news = NewsData.builder()
                    .title(newsNode.get("headline").asText())
                    .source(newsNode.get("source").asText())
                    .url(newsNode.get("url").asText())
                    .publishedAt(LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(newsNode.get("datetime").asLong()),
                            ZoneId.systemDefault()))
                    .summary(newsNode.get("summary").asText())
                    .sentimentScore(null) // Finnhub不直接提供情绪分数
                    .build();
            
            newsList.add(news);
        }
        
        log.info("从Finnhub获取到 {} 条新闻", newsList.size());
        return newsList;
    }
    
    @Override
    public boolean isEnabled() {
        return config.getFinnhub().isEnabled() && 
               config.getFinnhub().getApiKey() != null && 
               !config.getFinnhub().getApiKey().isEmpty();
    }
    
    @Override
    public String getProviderName() {
        return "Finnhub";
    }
}
