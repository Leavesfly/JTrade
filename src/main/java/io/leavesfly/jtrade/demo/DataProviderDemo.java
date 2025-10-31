package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.config.DataSourceConfig;
import io.leavesfly.jtrade.dataflow.model.FundamentalData;
import io.leavesfly.jtrade.dataflow.model.MarketData;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
import io.leavesfly.jtrade.dataflow.provider.FinnhubDataProvider;
import io.leavesfly.jtrade.dataflow.provider.YahooFinanceDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据提供者演示程序
 * 
 * 展示如何使用改进后的数据提供者功能：
 * 1. Yahoo Finance 市场数据获取
 * 2. 基本面数据获取（使用Yahoo Finance API）
 * 3. 社交媒体情绪分析（基于新闻数据）
 * 4. 技术指标计算（RSI, MACD, SMA, Bollinger Bands）
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class DataProviderDemo implements CommandLineRunner {

    private final DataAggregator dataAggregator;
    private final YahooFinanceDataProvider yahooProvider;
    private final FinnhubDataProvider finnhubProvider;

    public DataProviderDemo(DataAggregator dataAggregator,
                           YahooFinanceDataProvider yahooProvider,
                           FinnhubDataProvider finnhubProvider) {
        this.dataAggregator = dataAggregator;
        this.yahooProvider = yahooProvider;
        this.finnhubProvider = finnhubProvider;
    }

    public static void main(String[] args) {
        SpringApplication.run(DataProviderDemo.class, args);
    }

    @Override
    public void run(String... args) {
        printBanner();
        
        // 测试股票代码
        String[] symbols = {"AAPL", "MSFT", "GOOGL"};
        
        for (String symbol : symbols) {
            demonstrateDataProvider(symbol);
            System.out.println("\n" + "=".repeat(100) + "\n");
        }
    }

    /**
     * 演示单个股票的数据获取功能
     */
    private void demonstrateDataProvider(String symbol) {
        System.out.println("📊 正在分析股票: " + symbol);
        System.out.println("-".repeat(100));
        
        // 1. 获取市场数据
        demonstrateMarketData(symbol);
        
        // 2. 获取基本面数据
        demonstrateFundamentalData(symbol);
        
        // 3. 获取社交媒体情绪
        demonstrateSocialSentiment(symbol);
        
        // 4. 获取技术指标
        demonstrateTechnicalIndicators(symbol);
    }

    /**
     * 演示市场数据获取
     */
    private void demonstrateMarketData(String symbol) {
        System.out.println("\n📈 1. 市场数据 (Market Data)");
        System.out.println("-".repeat(100));
        
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(5);
            
            List<MarketData> marketData = dataAggregator.getMarketData(symbol, startDate, endDate);
            
            if (marketData.isEmpty()) {
                System.out.println("⚠️  未能获取市场数据");
                return;
            }
            
            System.out.printf("%-12s %-12s %-12s %-12s %-12s %-15s%n",
                    "日期", "开盘价", "最高价", "最低价", "收盘价", "成交量");
            System.out.println("-".repeat(100));
            
            // 显示最近5条数据
            int count = Math.min(5, marketData.size());
            for (int i = marketData.size() - count; i < marketData.size(); i++) {
                MarketData data = marketData.get(i);
                System.out.printf("%-12s $%-11.2f $%-11.2f $%-11.2f $%-11.2f %-15s%n",
                        data.getDate(),
                        data.getOpen(),
                        data.getHigh(),
                        data.getLow(),
                        data.getClose(),
                        String.format("%,d", data.getVolume()));
            }
            
            // 计算价格变化
            if (marketData.size() >= 2) {
                MarketData latest = marketData.get(marketData.size() - 1);
                MarketData previous = marketData.get(marketData.size() - 2);
                double change = latest.getClose().subtract(previous.getClose()).doubleValue();
                double changePercent = (change / previous.getClose().doubleValue()) * 100;
                
                String arrow = change >= 0 ? "📈 ↑" : "📉 ↓";
                System.out.printf("%n%s 价格变化: $%.2f (%.2f%%)%n", arrow, change, changePercent);
            }
            
        } catch (Exception e) {
            log.error("获取市场数据失败", e);
            System.out.println("❌ 获取市场数据时出错: " + e.getMessage());
        }
    }

    /**
     * 演示基本面数据获取
     */
    private void demonstrateFundamentalData(String symbol) {
        System.out.println("\n💼 2. 基本面数据 (Fundamental Data)");
        System.out.println("-".repeat(100));
        
        try {
            FundamentalData fundamental = dataAggregator.getFundamentalData(symbol);
            
            if (fundamental == null) {
                System.out.println("⚠️  未能获取基本面数据");
                return;
            }
            
            System.out.printf("公司名称: %s%n", fundamental.getCompanyName());
            System.out.printf("行业板块: %s / %s%n", 
                    fundamental.getSector() != null ? fundamental.getSector() : "N/A",
                    fundamental.getIndustry() != null ? fundamental.getIndustry() : "N/A");
            System.out.println("-".repeat(100));
            
            System.out.printf("市值:         $%,d%n", 
                    fundamental.getMarketCap() != null ? fundamental.getMarketCap().longValue() : 0);
            System.out.printf("市盈率 (P/E): %.2f%n", 
                    fundamental.getPeRatio() != null ? fundamental.getPeRatio() : 0);
            System.out.printf("市净率 (P/B): %.2f%n", 
                    fundamental.getPbRatio() != null ? fundamental.getPbRatio() : 0);
            System.out.printf("股息收益率:   %.2f%%%n", 
                    fundamental.getDividendYield() != null ? fundamental.getDividendYield() : 0);
            System.out.printf("每股收益 (EPS): $%.2f%n", 
                    fundamental.getEps() != null ? fundamental.getEps() : 0);
            
            if (fundamental.getRoe() != null) {
                System.out.printf("净资产收益率 (ROE): %.2f%%%n", 
                        fundamental.getRoe().doubleValue() * 100);
            }
            
        } catch (Exception e) {
            log.error("获取基本面数据失败", e);
            System.out.println("❌ 获取基本面数据时出错: " + e.getMessage());
        }
    }

    /**
     * 演示社交媒体情绪分析
     */
    private void demonstrateSocialSentiment(String symbol) {
        System.out.println("\n💬 3. 社交媒体情绪分析 (Social Media Sentiment)");
        System.out.println("-".repeat(100));
        
        try {
            Map<String, Object> sentiment = dataAggregator.getSocialMediaSentiment(symbol);
            
            double overallSentiment = (Double) sentiment.get("overall_sentiment");
            double positiveRatio = (Double) sentiment.get("positive_ratio");
            double negativeRatio = (Double) sentiment.get("negative_ratio");
            int postCount = (Integer) sentiment.get("post_count");
            
            // 情绪评级
            String sentimentLevel;
            String emoji;
            if (overallSentiment > 0.5) {
                sentimentLevel = "非常积极";
                emoji = "😄";
            } else if (overallSentiment > 0.2) {
                sentimentLevel = "积极";
                emoji = "🙂";
            } else if (overallSentiment > -0.2) {
                sentimentLevel = "中性";
                emoji = "😐";
            } else if (overallSentiment > -0.5) {
                sentimentLevel = "消极";
                emoji = "🙁";
            } else {
                sentimentLevel = "非常消极";
                emoji = "😟";
            }
            
            System.out.printf("总体情绪: %.3f %s %s%n", overallSentiment, emoji, sentimentLevel);
            System.out.println("-".repeat(100));
            System.out.printf("分析帖子数量: %d%n", postCount);
            System.out.printf("积极情绪比例: %.1f%%%n", positiveRatio * 100);
            System.out.printf("消极情绪比例: %.1f%%%n", negativeRatio * 100);
            
            // 可视化情绪分布
            System.out.println("\n情绪分布:");
            printBar("积极", positiveRatio, "🟢");
            printBar("消极", negativeRatio, "🔴");
            
        } catch (Exception e) {
            log.error("获取社交媒体情绪失败", e);
            System.out.println("❌ 获取社交媒体情绪时出错: " + e.getMessage());
        }
    }

    /**
     * 演示技术指标计算
     */
    private void demonstrateTechnicalIndicators(String symbol) {
        System.out.println("\n📊 4. 技术指标 (Technical Indicators)");
        System.out.println("-".repeat(100));
        
        try {
            Map<String, Double> indicators = dataAggregator.getTechnicalIndicators(symbol);
            
            // RSI 分析
            double rsi = indicators.get("RSI");
            String rsiSignal;
            if (rsi > 70) {
                rsiSignal = "超买 (Overbought) 🔴";
            } else if (rsi < 30) {
                rsiSignal = "超卖 (Oversold) 🟢";
            } else {
                rsiSignal = "中性 (Neutral) ⚪";
            }
            
            System.out.printf("RSI (14):     %.2f  %s%n", rsi, rsiSignal);
            
            // MACD 分析
            Double macd = indicators.get("MACD");
            Double macdSignal = indicators.get("MACD_Signal");
            Double macdHist = indicators.get("MACD_Histogram");
            
            if (macd != null && macdSignal != null && macdHist != null) {
                String macdTrend = macdHist > 0 ? "看涨 (Bullish) 📈" : "看跌 (Bearish) 📉";
                System.out.printf("MACD:         %.2f  %s%n", macd, macdTrend);
                System.out.printf("MACD Signal:  %.2f%n", macdSignal);
                System.out.printf("MACD Hist:    %.2f%n", macdHist);
            }
            
            // 移动平均线分析
            double sma20 = indicators.get("SMA_20");
            double sma50 = indicators.get("SMA_50");
            String maTrend = sma20 > sma50 ? "短期上涨趋势 📈" : "短期下跌趋势 📉";
            
            System.out.println("-".repeat(100));
            System.out.printf("SMA (20日):   $%.2f%n", sma20);
            System.out.printf("SMA (50日):   $%.2f  %s%n", sma50, maTrend);
            
            // 布林带分析
            Double bbUpper = indicators.get("BB_UPPER");
            Double bbMiddle = indicators.get("BB_MIDDLE");
            Double bbLower = indicators.get("BB_LOWER");
            
            if (bbUpper != null && bbMiddle != null && bbLower != null) {
                System.out.println("-".repeat(100));
                System.out.printf("布林带上轨:   $%.2f%n", bbUpper);
                System.out.printf("布林带中轨:   $%.2f%n", bbMiddle);
                System.out.printf("布林带下轨:   $%.2f%n", bbLower);
                System.out.printf("波动范围:     $%.2f%n", bbUpper - bbLower);
            }
            
            // 综合信号
            System.out.println("\n" + "-".repeat(100));
            System.out.println("💡 综合交易信号:");
            generateTradingSignal(rsi, macdHist, sma20, sma50);
            
        } catch (Exception e) {
            log.error("获取技术指标失败", e);
            System.out.println("❌ 获取技术指标时出错: " + e.getMessage());
        }
    }

    /**
     * 生成综合交易信号
     */
    private void generateTradingSignal(double rsi, Double macdHist, double sma20, double sma50) {
        int bullishSignals = 0;
        int bearishSignals = 0;
        
        // RSI 信号
        if (rsi < 30) bullishSignals++;
        if (rsi > 70) bearishSignals++;
        
        // MACD 信号
        if (macdHist != null) {
            if (macdHist > 0) bullishSignals++;
            if (macdHist < 0) bearishSignals++;
        }
        
        // MA 信号
        if (sma20 > sma50) bullishSignals++;
        if (sma20 < sma50) bearishSignals++;
        
        if (bullishSignals > bearishSignals) {
            System.out.println("📈 看涨信号 (BULLISH) - 考虑买入");
        } else if (bearishSignals > bullishSignals) {
            System.out.println("📉 看跌信号 (BEARISH) - 考虑卖出或观望");
        } else {
            System.out.println("⚖️  中性信号 (NEUTRAL) - 保持观望");
        }
        
        System.out.printf("   看涨指标: %d | 看跌指标: %d%n", bullishSignals, bearishSignals);
    }

    /**
     * 打印条形图
     */
    private void printBar(String label, double ratio, String symbol) {
        int barLength = (int) (ratio * 50);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            bar.append(symbol);
        }
        System.out.printf("  %-6s [%-50s] %.1f%%%n", label, bar.toString(), ratio * 100);
    }

    /**
     * 打印演示横幅
     */
    private void printBanner() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("  ____        _        ____                 _     _             ____                        ");
        System.out.println(" |  _ \\  __ _| |_ __ _|  _ \\ _ __ _____   _(_) __| | ___ _ __  |  _ \\  ___ _ __ ___   ___  ");
        System.out.println(" | | | |/ _` | __/ _` | |_) | '__/ _ \\ \\ / / |/ _` |/ _ \\ '__| | | | |/ _ \\ '_ ` _ \\ / _ \\ ");
        System.out.println(" | |_| | (_| | || (_| |  __/| | | (_) \\ V /| | (_| |  __/ |    | |_| |  __/ | | | | | (_) |");
        System.out.println(" |____/ \\__,_|\\__\\__,_|_|   |_|  \\___/ \\_/ |_|\\__,_|\\___|_|    |____/ \\___|_| |_| |_|\\___/ ");
        System.out.println("=".repeat(100));
        System.out.println("  JTrade 数据提供者功能演示");
        System.out.println("  展示 Yahoo Finance、基本面分析、情绪分析和技术指标功能");
        System.out.println("=".repeat(100) + "\n");
    }
}
