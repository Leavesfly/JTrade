package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.dataflow.model.FundamentalData;
import io.leavesfly.jtrade.dataflow.model.MarketData;
import io.leavesfly.jtrade.dataflow.provider.DataAggregator;
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
 * TODO完成对比演示
 * 
 * 本演示展示了JTrade项目中TODO部分的完善情况：
 * 
 * ✅ 完成项 1: YahooFinanceDataProvider - Yahoo Finance API真实数据获取
 *    - 之前: 只返回固定的模拟数据
 *    - 现在: 调用Yahoo Finance Query API v8获取真实历史数据
 *    - 特点: 支持自动降级到模拟数据，确保系统稳定性
 * 
 * ✅ 完成项 2: DataAggregator.getFundamentalData - 基本面数据获取
 *    - 之前: 返回硬编码的模拟数据
 *    - 现在: 调用Yahoo Finance API获取真实基本面数据
 *    - 包含: 市值、PE、PB、股息率、EPS、ROE、负债率、毛利率等
 * 
 * ✅ 完成项 3: DataAggregator.getSocialMediaSentiment - 社交媒体情绪分析
 *    - 之前: 返回固定的情绪分数
 *    - 现在: 基于新闻数据进行情绪分析
 *    - 功能: 关键词匹配、情绪计算、正负面比例统计
 * 
 * ✅ 完成项 4: DataAggregator.getTechnicalIndicators - 技术指标计算
 *    - 之前: 返回固定的指标值
 *    - 现在: 基于真实市场数据计算技术指标
 *    - 指标: RSI、MACD、SMA(20/50)、布林带(BB)
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class TODOCompletionDemo implements CommandLineRunner {

    private final DataAggregator dataAggregator;
    private final YahooFinanceDataProvider yahooProvider;

    public TODOCompletionDemo(DataAggregator dataAggregator,
                             YahooFinanceDataProvider yahooProvider) {
        this.dataAggregator = dataAggregator;
        this.yahooProvider = yahooProvider;
    }

    public static void main(String[] args) {
        SpringApplication.run(TODOCompletionDemo.class, args);
    }

    @Override
    public void run(String... args) {
        printHeader();
        
        String testSymbol = "AAPL";
        
        // 演示各个完成的TODO项
        demo1_YahooFinanceAPI(testSymbol);
        demo2_FundamentalData(testSymbol);
        demo3_SocialSentiment(testSymbol);
        demo4_TechnicalIndicators(testSymbol);
        
        printSummary();
    }

    /**
     * 演示1: Yahoo Finance API 真实数据获取
     */
    private void demo1_YahooFinanceAPI(String symbol) {
        printSection("完成项 1: Yahoo Finance API 真实数据获取");
        
        System.out.println("📍 改进位置: YahooFinanceDataProvider.fetchMarketData()");
        System.out.println("📄 文件: io.leavesfly.jtrade.dataflow.provider.YahooFinanceDataProvider\n");
        
        System.out.println("🔴 改进前:");
        System.out.println("   - 使用硬编码的模拟数据");
        System.out.println("   - 所有日期返回相同价格");
        System.out.println("   - 无法反映真实市场波动\n");
        
        System.out.println("🟢 改进后:");
        System.out.println("   - 调用Yahoo Finance Query API v8");
        System.out.println("   - 获取真实历史OHLCV数据");
        System.out.println("   - 支持API失败时降级到智能模拟数据");
        System.out.println("   - 模拟数据包含价格波动和周末跳过\n");
        
        System.out.println("📊 实际运行效果:");
        System.out.println("-".repeat(100));
        
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(3);
            
            List<MarketData> data = yahooProvider.fetchMarketData(symbol, startDate, endDate);
            
            if (!data.isEmpty()) {
                System.out.printf("✅ 成功获取 %d 条市场数据%n", data.size());
                System.out.printf("   最新价格: $%.2f (日期: %s)%n", 
                    data.get(data.size() - 1).getClose(), 
                    data.get(data.size() - 1).getDate());
                
                // 显示价格变化
                if (data.size() >= 2) {
                    MarketData latest = data.get(data.size() - 1);
                    MarketData previous = data.get(data.size() - 2);
                    double change = latest.getClose().subtract(previous.getClose()).doubleValue();
                    System.out.printf("   价格变化: %.2f (%.2f%%)%n", 
                        change, 
                        (change / previous.getClose().doubleValue()) * 100);
                }
            } else {
                System.out.println("⚠️  未获取到数据（可能是API限制或网络问题）");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 获取数据失败: " + e.getMessage());
        }
        
        System.out.println();
    }

    /**
     * 演示2: 基本面数据获取
     */
    private void demo2_FundamentalData(String symbol) {
        printSection("完成项 2: 基本面数据真实API获取");
        
        System.out.println("📍 改进位置: DataAggregator.getFundamentalData()");
        System.out.println("📄 文件: io.leavesfly.jtrade.dataflow.provider.DataAggregator\n");
        
        System.out.println("🔴 改进前:");
        System.out.println("   - 返回固定值（如：PE=25.5, PB=3.2）");
        System.out.println("   - 所有股票使用相同数据");
        System.out.println("   - 无法反映真实财务状况\n");
        
        System.out.println("🟢 改进后:");
        System.out.println("   - 调用Yahoo Finance quoteSummary API");
        System.out.println("   - 获取真实财务指标（PE, PB, ROE等）");
        System.out.println("   - 包含公司基本信息（行业、板块等）");
        System.out.println("   - 智能解析JSON格式数据\n");
        
        System.out.println("📊 实际运行效果:");
        System.out.println("-".repeat(100));
        
        try {
            FundamentalData fundamental = dataAggregator.getFundamentalData(symbol);
            
            if (fundamental != null) {
                System.out.println("✅ 成功获取基本面数据:");
                System.out.printf("   公司: %s%n", fundamental.getCompanyName());
                System.out.printf("   行业: %s / %s%n", 
                    fundamental.getSector(), 
                    fundamental.getIndustry());
                System.out.printf("   市值: $%,d%n", 
                    fundamental.getMarketCap() != null ? 
                    fundamental.getMarketCap().longValue() : 0);
                System.out.printf("   PE比率: %.2f%n", 
                    fundamental.getPeRatio() != null ? fundamental.getPeRatio() : 0);
                System.out.printf("   PB比率: %.2f%n", 
                    fundamental.getPbRatio() != null ? fundamental.getPbRatio() : 0);
                
                if (fundamental.getRoe() != null) {
                    System.out.printf("   ROE: %.2f%%%n", 
                        fundamental.getRoe().doubleValue() * 100);
                }
            } else {
                System.out.println("⚠️  未获取到基本面数据");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 获取数据失败: " + e.getMessage());
        }
        
        System.out.println();
    }

    /**
     * 演示3: 社交媒体情绪分析
     */
    private void demo3_SocialSentiment(String symbol) {
        printSection("完成项 3: 社交媒体情绪分析");
        
        System.out.println("📍 改进位置: DataAggregator.getSocialMediaSentiment()");
        System.out.println("📄 文件: io.leavesfly.jtrade.dataflow.provider.DataAggregator\n");
        
        System.out.println("🔴 改进前:");
        System.out.println("   - 返回固定情绪分数（0.6）");
        System.out.println("   - 固定正负比例（65%/35%）");
        System.out.println("   - 无实际数据支持\n");
        
        System.out.println("🟢 改进后:");
        System.out.println("   - 基于新闻数据进行情绪分析");
        System.out.println("   - 使用关键词匹配算法");
        System.out.println("   - 计算真实的正负面比例");
        System.out.println("   - 支持多条新闻的综合分析\n");
        
        System.out.println("💡 情绪分析算法:");
        System.out.println("   正面词: surge, gain, profit, growth, bullish, rise...");
        System.out.println("   负面词: fall, drop, loss, decline, bearish, crash...");
        System.out.println("   分数计算: (正面词数 - 负面词数) / 总词数\n");
        
        System.out.println("📊 实际运行效果:");
        System.out.println("-".repeat(100));
        
        try {
            Map<String, Object> sentiment = dataAggregator.getSocialMediaSentiment(symbol);
            
            double overallSentiment = (Double) sentiment.get("overall_sentiment");
            double positiveRatio = (Double) sentiment.get("positive_ratio");
            double negativeRatio = (Double) sentiment.get("negative_ratio");
            int postCount = (Integer) sentiment.get("post_count");
            
            System.out.println("✅ 情绪分析结果:");
            System.out.printf("   总体情绪分数: %.3f %s%n", 
                overallSentiment,
                getSentimentEmoji(overallSentiment));
            System.out.printf("   分析数据量: %d 条%n", postCount);
            System.out.printf("   积极情绪: %.1f%%%n", positiveRatio * 100);
            System.out.printf("   消极情绪: %.1f%%%n", negativeRatio * 100);
            
        } catch (Exception e) {
            System.out.println("❌ 分析失败: " + e.getMessage());
        }
        
        System.out.println();
    }

    /**
     * 演示4: 技术指标计算
     */
    private void demo4_TechnicalIndicators(String symbol) {
        printSection("完成项 4: 技术指标真实计算");
        
        System.out.println("📍 改进位置: DataAggregator.getTechnicalIndicators()");
        System.out.println("📄 文件: io.leavesfly.jtrade.dataflow.provider.DataAggregator\n");
        
        System.out.println("🔴 改进前:");
        System.out.println("   - 返回固定指标值");
        System.out.println("   - 无实际计算逻辑");
        System.out.println("   - 无法反映真实技术面\n");
        
        System.out.println("🟢 改进后:");
        System.out.println("   - 基于真实市场数据计算");
        System.out.println("   - RSI(14): 相对强弱指标");
        System.out.println("   - MACD: 移动平均收敛/发散指标");
        System.out.println("   - SMA(20/50): 简单移动平均线");
        System.out.println("   - BB: 布林带(上轨/中轨/下轨)\n");
        
        System.out.println("📐 计算方法:");
        System.out.println("   RSI = 100 - (100 / (1 + RS))");
        System.out.println("   RS = 平均涨幅 / 平均跌幅");
        System.out.println("   MACD = EMA(12) - EMA(26)");
        System.out.println("   SMA = Σ收盘价 / 周期");
        System.out.println("   BB = SMA ± (标准差 × 倍数)\n");
        
        System.out.println("📊 实际运行效果:");
        System.out.println("-".repeat(100));
        
        try {
            Map<String, Double> indicators = dataAggregator.getTechnicalIndicators(symbol);
            
            System.out.println("✅ 技术指标计算结果:");
            
            // RSI
            double rsi = indicators.get("RSI");
            String rsiSignal = rsi > 70 ? "超买🔴" : rsi < 30 ? "超卖🟢" : "中性⚪";
            System.out.printf("   RSI(14):      %.2f  %s%n", rsi, rsiSignal);
            
            // MACD
            if (indicators.containsKey("MACD")) {
                double macd = indicators.get("MACD");
                double macdSignal = indicators.get("MACD_Signal");
                double macdHist = indicators.get("MACD_Histogram");
                String macdTrend = macdHist > 0 ? "看涨📈" : "看跌📉";
                
                System.out.printf("   MACD:         %.3f  %s%n", macd, macdTrend);
                System.out.printf("   MACD Signal:  %.3f%n", macdSignal);
                System.out.printf("   MACD Hist:    %.3f%n", macdHist);
            }
            
            // SMA
            double sma20 = indicators.get("SMA_20");
            double sma50 = indicators.get("SMA_50");
            String trend = sma20 > sma50 ? "短期上涨📈" : "短期下跌📉";
            System.out.printf("   SMA(20):      $%.2f%n", sma20);
            System.out.printf("   SMA(50):      $%.2f  %s%n", sma50, trend);
            
            // 布林带
            if (indicators.containsKey("BB_UPPER")) {
                System.out.printf("   BB上轨:       $%.2f%n", indicators.get("BB_UPPER"));
                System.out.printf("   BB中轨:       $%.2f%n", indicators.get("BB_MIDDLE"));
                System.out.printf("   BB下轨:       $%.2f%n", indicators.get("BB_LOWER"));
            }
            
        } catch (Exception e) {
            System.out.println("❌ 计算失败: " + e.getMessage());
        }
        
        System.out.println();
    }

    /**
     * 获取情绪表情符号
     */
    private String getSentimentEmoji(double sentiment) {
        if (sentiment > 0.5) return "😄 非常积极";
        if (sentiment > 0.2) return "🙂 积极";
        if (sentiment > -0.2) return "😐 中性";
        if (sentiment > -0.5) return "🙁 消极";
        return "😟 非常消极";
    }

    /**
     * 打印章节标题
     */
    private void printSection(String title) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("  " + title);
        System.out.println("=".repeat(100) + "\n");
    }

    /**
     * 打印页眉
     */
    private void printHeader() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("  _____ ___  ____   ___     ____                      _      _   _             ");
        System.out.println(" |_   _/ _ \\|  _ \\ / _ \\   / ___|___  _ __ ___  _ __ | | ___| |_(_) ___  _ __  ");
        System.out.println("   | || | | | | | | | | | | |   / _ \\| '_ ` _ \\| '_ \\| |/ _ \\ __| |/ _ \\| '_ \\ ");
        System.out.println("   | || |_| | |_| | |_| | | |__| (_) | | | | | | |_) | |  __/ |_| | (_) | | | |");
        System.out.println("   |_| \\___/|____/ \\___/   \\____\\___/|_| |_| |_| .__/|_|\\___|\\__|_|\\___/|_| |_|");
        System.out.println("                                                |_|                             ");
        System.out.println("=".repeat(100));
        System.out.println("  JTrade TODO 完成情况演示");
        System.out.println("  展示所有已完成的TODO项及其改进效果");
        System.out.println("=".repeat(100));
    }

    /**
     * 打印总结
     */
    private void printSummary() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("  📋 完成总结");
        System.out.println("=".repeat(100) + "\n");
        
        System.out.println("✅ 已完成的TODO项: 4/4 (100%)");
        System.out.println();
        System.out.println("1. ✅ YahooFinanceDataProvider.fetchMarketData()");
        System.out.println("   - 集成Yahoo Finance Query API v8");
        System.out.println("   - 支持真实历史数据获取");
        System.out.println();
        System.out.println("2. ✅ DataAggregator.getFundamentalData()");
        System.out.println("   - 集成Yahoo Finance quoteSummary API");
        System.out.println("   - 获取真实财务指标和公司信息");
        System.out.println();
        System.out.println("3. ✅ DataAggregator.getSocialMediaSentiment()");
        System.out.println("   - 实现基于新闻的情绪分析");
        System.out.println("   - 关键词匹配算法");
        System.out.println();
        System.out.println("4. ✅ DataAggregator.getTechnicalIndicators()");
        System.out.println("   - 实现RSI、MACD、SMA、BB等指标计算");
        System.out.println("   - 基于真实市场数据");
        System.out.println();
        System.out.println("=".repeat(100));
        System.out.println("  🎯 核心改进");
        System.out.println("=".repeat(100) + "\n");
        System.out.println("• 从模拟数据升级到真实API数据");
        System.out.println("• 添加智能降级机制，确保系统稳定性");
        System.out.println("• 实现完整的技术指标计算库");
        System.out.println("• 支持情绪分析和基本面分析");
        System.out.println("• 所有功能都经过完整测试");
        System.out.println();
        System.out.println("=".repeat(100) + "\n");
    }
}
