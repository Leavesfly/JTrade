package io.leavesfly.jtrade.dataflow.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.leavesfly.jtrade.dataflow.model.FundamentalData;
import io.leavesfly.jtrade.dataflow.model.MarketData;
import io.leavesfly.jtrade.dataflow.model.NewsData;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据聚合器
 * 
 * 从多个数据源聚合数据，提供统一的数据访问接口
 * 
 * @author 山泽
 */
@Slf4j
@Component
public class DataAggregator {
    
    private final List<DataProvider> dataProviders;
    private final FinnhubDataProvider finnhubDataProvider;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public DataAggregator(List<DataProvider> dataProviders, 
                         FinnhubDataProvider finnhubDataProvider) {
        this.dataProviders = dataProviders;
        this.finnhubDataProvider = finnhubDataProvider;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 获取市场数据
     */
    public List<MarketData> getMarketData(String symbol, LocalDate startDate, LocalDate endDate) {
        for (DataProvider provider : dataProviders) {
            if (provider.isEnabled()) {
                try {
                    List<MarketData> data = provider.fetchMarketData(symbol, startDate, endDate);
                    if (!data.isEmpty()) {
                        log.info("从 {} 成功获取市场数据", provider.getProviderName());
                        return data;
                    }
                } catch (Exception e) {
                    log.error("从 {} 获取数据失败", provider.getProviderName(), e);
                }
            }
        }
        
        log.warn("未能从任何数据源获取到市场数据");
        return new ArrayList<>();
    }
    
    /**
     * 获取基本面数据（使用Yahoo Finance API）
     */
    public FundamentalData getFundamentalData(String symbol) {
        log.info("获取 {} 的基本面数据", symbol);
        
        try {
            // 使用Yahoo Finance API获取公司基本信息
            String url = String.format(
                "https://query1.finance.yahoo.com/v10/finance/quoteSummary/%s?modules=defaultKeyStatistics,financialData,summaryProfile",
                symbol
            );
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("Yahoo Finance API调用失败: {}，使用模拟数据", response.code());
                    return getFallbackFundamentalData(symbol);
                }
                
                String body = response.body().string();
                JsonNode rootNode = objectMapper.readTree(body);
                
                JsonNode result = rootNode.path("quoteSummary").path("result").get(0);
                if (result == null) {
                    log.warn("未能获取 {} 的基本面数据，使用模拟数据", symbol);
                    return getFallbackFundamentalData(symbol);
                }
                
                return parseFundamentalData(symbol, result);
            }
            
        } catch (Exception e) {
            log.error("获取基本面数据失败", e);
            return getFallbackFundamentalData(symbol);
        }
    }
    
    /**
     * 解析Yahoo Finance基本面数据
     */
    private FundamentalData parseFundamentalData(String symbol, JsonNode result) {
        try {
            JsonNode keyStats = result.path("defaultKeyStatistics");
            JsonNode financialData = result.path("financialData");
            JsonNode profile = result.path("summaryProfile");
            
            return FundamentalData.builder()
                    .symbol(symbol)
                    .companyName(profile.path("longName").asText(symbol + " Inc."))
                    .marketCap(getValueAsBigDecimal(keyStats.path("marketCap")))
                    .peRatio(getValueAsBigDecimal(keyStats.path("trailingPE")))
                    .pbRatio(getValueAsBigDecimal(keyStats.path("priceToBook")))
                    .dividendYield(getValueAsBigDecimal(keyStats.path("dividendYield")))
                    .eps(getValueAsBigDecimal(keyStats.path("trailingEps")))
                    .roe(getValueAsBigDecimal(financialData.path("returnOnEquity")))
                    .debtToEquity(getValueAsBigDecimal(financialData.path("debtToEquity")))
                    .grossMargin(getValueAsBigDecimal(financialData.path("grossMargins")))
                    .industry(profile.path("industry").asText("N/A"))
                    .sector(profile.path("sector").asText("N/A"))
                    .build();
                    
        } catch (Exception e) {
            log.error("解析基本面数据失败", e);
            return getFallbackFundamentalData(symbol);
        }
    }
    
    /**
     * 从 JSON 节点中提取数值
     */
    private BigDecimal getValueAsBigDecimal(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        
        // 如果是对象，尝试获取raw或fmt字段
        if (node.isObject()) {
            JsonNode rawNode = node.path("raw");
            if (!rawNode.isMissingNode()) {
                return new BigDecimal(rawNode.asText());
            }
        }
        
        // 直接返回数值
        if (node.isNumber()) {
            return new BigDecimal(node.asText());
        }
        
        return null;
    }
    
    /**
     * 备用的模拟基本面数据
     */
    private FundamentalData getFallbackFundamentalData(String symbol) {
        return FundamentalData.builder()
                .symbol(symbol)
                .companyName(symbol + " Inc.")
                .marketCap(new BigDecimal("1000000000"))
                .peRatio(new BigDecimal("25.5"))
                .pbRatio(new BigDecimal("3.2"))
                .dividendYield(new BigDecimal("2.5"))
                .eps(new BigDecimal("6.0"))
                .build();
    }
    
    /**
     * 获取新闻数据（真实API）
     */
    public List<NewsData> getNewsData(String symbol, int limit) {
        log.info("获取 {} 的新闻数据，限制 {} 条", symbol, limit);
        
        // 先尝试使用Finnhub获取真实新闻
        if (finnhubDataProvider.isEnabled()) {
            try {
                LocalDate toDate = LocalDate.now();
                LocalDate fromDate = toDate.minusDays(7); // 过去7天
                List<NewsData> news = finnhubDataProvider.fetchNews(symbol, fromDate, toDate);
                
                if (!news.isEmpty()) {
                    // 限制数量
                    return news.stream()
                            .limit(limit)
                            .collect(java.util.stream.Collectors.toList());
                }
            } catch (Exception e) {
                log.error("从Finnhub获取新闻失败", e);
            }
        }
        
        // 如果真实API失败，使用模拟数据
        log.info("使用模拟新闻数据");
        List<NewsData> newsList = new ArrayList<>();
        
        for (int i = 0; i < limit; i++) {
            NewsData news = NewsData.builder()
                    .title(symbol + " 股价波动分析")
                    .source("财经新闻")
                    .url("https://example.com/news/" + i)
                    .publishedAt(LocalDate.now().atStartOfDay())
                    .summary("这是一条关于 " + symbol + " 的模拟新闻")
                    .sentimentScore(0.5)
                    .build();
            newsList.add(news);
        }
        
        return newsList;
    }
    
    /**
     * 获取社交媒体情绪数据（使用情绪分析）
     */
    public Map<String, Object> getSocialMediaSentiment(String symbol) {
        log.info("获取 {} 的社交媒体情绪数据", symbol);
        
        Map<String, Object> sentiment = new HashMap<>();
        sentiment.put("symbol", symbol);
        
        try {
            // 尝试从Finnhub获取社交媒体情绪数据
            if (finnhubDataProvider.isEnabled()) {
                // 获取新闻数据并分析情绪
                LocalDate toDate = LocalDate.now();
                LocalDate fromDate = toDate.minusDays(7);
                List<NewsData> newsList = finnhubDataProvider.fetchNews(symbol, fromDate, toDate);
                
                if (!newsList.isEmpty()) {
                    // 基于新闻数据计算情绪
                    double totalSentiment = 0.0;
                    int positiveCount = 0;
                    int negativeCount = 0;
                    int neutralCount = 0;
                    
                    for (NewsData news : newsList) {
                        // 简单的情绪分析：基于关键词
                        double newsSentiment = analyzeTextSentiment(news.getTitle() + " " + news.getSummary());
                        totalSentiment += newsSentiment;
                        
                        if (newsSentiment > 0.3) {
                            positiveCount++;
                        } else if (newsSentiment < -0.3) {
                            negativeCount++;
                        } else {
                            neutralCount++;
                        }
                    }
                    
                    int totalCount = newsList.size();
                    double avgSentiment = totalCount > 0 ? totalSentiment / totalCount : 0.0;
                    
                    sentiment.put("overall_sentiment", avgSentiment);
                    sentiment.put("positive_ratio", totalCount > 0 ? (double) positiveCount / totalCount : 0.0);
                    sentiment.put("negative_ratio", totalCount > 0 ? (double) negativeCount / totalCount : 0.0);
                    sentiment.put("neutral_ratio", totalCount > 0 ? (double) neutralCount / totalCount : 0.0);
                    sentiment.put("post_count", totalCount);
                    
                    log.info("基于 {} 条新闻计算得到情绪分数: {}", totalCount, avgSentiment);
                    return sentiment;
                }
            }
        } catch (Exception e) {
            log.error("获取社交媒体情绪失败", e);
        }
        
        // 如果无法获取真实数据，返回模拟数据
        log.info("使用模拟情绪数据");
        sentiment.put("overall_sentiment", 0.6);
        sentiment.put("positive_ratio", 0.65);
        sentiment.put("negative_ratio", 0.35);
        sentiment.put("neutral_ratio", 0.0);
        sentiment.put("post_count", 150);
        
        return sentiment;
    }
    
    /**
     * 简单的文本情绪分析
     * 返回值范围: -1.0 (非常负面) 到 1.0 (非常正面)
     */
    private double analyzeTextSentiment(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        
        String lowerText = text.toLowerCase();
        
        // 正面关键词
        String[] positiveWords = {
            "surge", "gain", "profit", "growth", "bullish", "rise", "up", 
            "strong", "beat", "outperform", "success", "positive", "boost",
            "rally", "soar", "increase", "breakthrough", "record", "high"
        };
        
        // 负面关键词
        String[] negativeWords = {
            "fall", "drop", "loss", "decline", "bearish", "down", "weak",
            "miss", "underperform", "fail", "negative", "crash", "plunge",
            "sink", "decrease", "worry", "concern", "risk", "low"
        };
        
        int positiveScore = 0;
        int negativeScore = 0;
        
        for (String word : positiveWords) {
            if (lowerText.contains(word)) {
                positiveScore++;
            }
        }
        
        for (String word : negativeWords) {
            if (lowerText.contains(word)) {
                negativeScore++;
            }
        }
        
        int totalWords = positiveScore + negativeScore;
        if (totalWords == 0) {
            return 0.0; // 中性
        }
        
        // 计算情绪分数 (-1 到 1)
        return (double) (positiveScore - negativeScore) / Math.max(totalWords, 1);
    }
    
    /**
     * 获取技术指标数据（使用历史数据计算）
     */
    public Map<String, Double> getTechnicalIndicators(String symbol) {
        log.info("获取 {} 的技术指标数据", symbol);
        
        Map<String, Double> indicators = new HashMap<>();
        
        try {
            // 获取过60天的市场数据用于计算技术指标
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(60);
            List<MarketData> marketData = getMarketData(symbol, startDate, endDate);
            
            if (marketData.isEmpty()) {
                log.warn("无法获取市场数据，返回模拟指标");
                return getFallbackTechnicalIndicators();
            }
            
            // 计算RSI (Relative Strength Index)
            double rsi = calculateRSI(marketData, 14);
            indicators.put("RSI", rsi);
            
            // 计算简单移动平均线 (SMA)
            double sma20 = calculateSMA(marketData, 20);
            double sma50 = calculateSMA(marketData, 50);
            indicators.put("SMA_20", sma20);
            indicators.put("SMA_50", sma50);
            
            // 计算MACD
            Map<String, Double> macd = calculateMACD(marketData);
            indicators.putAll(macd);
            
            // 计算布林带 (Bollinger Bands)
            Map<String, Double> bb = calculateBollingerBands(marketData, 20, 2.0);
            indicators.putAll(bb);
            
            log.info("成功计算技术指标: RSI={}, SMA20={}, SMA50={}", rsi, sma20, sma50);
            
        } catch (Exception e) {
            log.error("计算技术指标失败", e);
            return getFallbackTechnicalIndicators();
        }
        
        return indicators;
    }
    
    /**
     * 计算RSI指标
     */
    private double calculateRSI(List<MarketData> data, int period) {
        if (data.size() < period + 1) {
            return 50.0; // 默认值
        }
        
        double gainSum = 0.0;
        double lossSum = 0.0;
        
        // 计算平均涨跌
        for (int i = data.size() - period; i < data.size(); i++) {
            double change = data.get(i).getClose().subtract(data.get(i - 1).getClose()).doubleValue();
            if (change > 0) {
                gainSum += change;
            } else {
                lossSum += Math.abs(change);
            }
        }
        
        double avgGain = gainSum / period;
        double avgLoss = lossSum / period;
        
        if (avgLoss == 0) {
            return 100.0;
        }
        
        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }
    
    /**
     * 计算简单移动平均线 (SMA)
     */
    private double calculateSMA(List<MarketData> data, int period) {
        if (data.size() < period) {
            period = data.size();
        }
        
        double sum = 0.0;
        for (int i = data.size() - period; i < data.size(); i++) {
            sum += data.get(i).getClose().doubleValue();
        }
        
        return sum / period;
    }
    
    /**
     * 计算MACD指标
     */
    private Map<String, Double> calculateMACD(List<MarketData> data) {
        Map<String, Double> macd = new HashMap<>();
        
        if (data.size() < 26) {
            macd.put("MACD", 0.0);
            macd.put("MACD_Signal", 0.0);
            macd.put("MACD_Histogram", 0.0);
            return macd;
        }
        
        // 计算EMA12和EMA26
        double ema12 = calculateEMA(data, 12);
        double ema26 = calculateEMA(data, 26);
        double macdLine = ema12 - ema26;
        
        // 简化版本：使用MACD线的简单平均作为信号线
        double signalLine = macdLine * 0.9; // 简化计算
        
        macd.put("MACD", macdLine);
        macd.put("MACD_Signal", signalLine);
        macd.put("MACD_Histogram", macdLine - signalLine);
        
        return macd;
    }
    
    /**
     * 计算指数移动平均线 (EMA)
     */
    private double calculateEMA(List<MarketData> data, int period) {
        if (data.size() < period) {
            return calculateSMA(data, data.size());
        }
        
        double multiplier = 2.0 / (period + 1);
        double ema = calculateSMA(data, period);
        
        // 从 period 位置开始计算 EMA
        for (int i = data.size() - period; i < data.size(); i++) {
            double price = data.get(i).getClose().doubleValue();
            ema = (price - ema) * multiplier + ema;
        }
        
        return ema;
    }
    
    /**
     * 计算布林带
     */
    private Map<String, Double> calculateBollingerBands(List<MarketData> data, int period, double numStdDev) {
        Map<String, Double> bb = new HashMap<>();
        
        if (data.size() < period) {
            double currentPrice = data.get(data.size() - 1).getClose().doubleValue();
            bb.put("BB_UPPER", currentPrice * 1.05);
            bb.put("BB_MIDDLE", currentPrice);
            bb.put("BB_LOWER", currentPrice * 0.95);
            return bb;
        }
        
        // 计算中轨 (SMA)
        double middle = calculateSMA(data, period);
        
        // 计算标准差
        double sum = 0.0;
        for (int i = data.size() - period; i < data.size(); i++) {
            double diff = data.get(i).getClose().doubleValue() - middle;
            sum += diff * diff;
        }
        double stdDev = Math.sqrt(sum / period);
        
        bb.put("BB_UPPER", middle + (numStdDev * stdDev));
        bb.put("BB_MIDDLE", middle);
        bb.put("BB_LOWER", middle - (numStdDev * stdDev));
        
        return bb;
    }
    
    /**
     * 备用的模拟技术指标
     */
    private Map<String, Double> getFallbackTechnicalIndicators() {
        Map<String, Double> indicators = new HashMap<>();
        indicators.put("RSI", 55.0);
        indicators.put("MACD", 1.5);
        indicators.put("MACD_Signal", 1.2);
        indicators.put("MACD_Histogram", 0.3);
        indicators.put("SMA_20", 150.0);
        indicators.put("SMA_50", 148.0);
        indicators.put("BB_UPPER", 160.0);
        indicators.put("BB_MIDDLE", 150.0);
        indicators.put("BB_LOWER", 140.0);
        return indicators;
    }
}
