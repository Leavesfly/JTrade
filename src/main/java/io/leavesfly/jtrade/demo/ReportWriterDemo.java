package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.stream.Stream;

/**
 * 报告写入功能演示
 * 
 * 展示如何将分析结果写入特定目录
 * - 按股票代码组织目录结构
 * - 生成完整的分析报告
 * - 中间结果和最终报告分离
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class ReportWriterDemo {
    
    public static void main(String[] args) {
        SpringApplication.run(ReportWriterDemo.class, args);
    }
    
    @Bean
    public CommandLineRunner reportWriterDemoRunner(TradingService tradingService) {
        return args -> {
            printBanner();
            
            // 演示1：单个股票分析并写入报告
            demoSingleStock(tradingService);
            
            // 演示2：多个股票分析并写入各自目录
            demoMultipleStocks(tradingService);
            
            // 展示生成的报告结构
            displayReportStructure();
        };
    }
    
    /**
     * 演示1：单个股票分析
     */
    private void demoSingleStock(TradingService tradingService) {
        log.info("\n" + "=".repeat(80));
        log.info("【演示1：单个股票分析 - AAPL】");
        log.info("=".repeat(80));
        
        String symbol = "AAPL";
        LocalDate date = LocalDate.now();
        
        log.info("开始分析 {}...", symbol);
        AgentState result = tradingService.executeTradingWorkflow(symbol, date);
        
        log.info("\n✓ 分析完成");
        log.info("  最终信号: {}", result.getFinalSignal());
        log.info("  报告位置: reports/{}/{}/", symbol, date);
    }
    
    /**
     * 演示2：多个股票分析
     */
    private void demoMultipleStocks(TradingService tradingService) {
        log.info("\n" + "=".repeat(80));
        log.info("【演示2：多个股票分析】");
        log.info("=".repeat(80));
        
        String[] stocks = {"TSLA", "MSFT", "GOOGL"};
        LocalDate date = LocalDate.now();
        
        for (String symbol : stocks) {
            log.info("\n分析 {}...", symbol);
            AgentState result = tradingService.executeTradingWorkflow(symbol, date);
            log.info("✓ {} 分析完成，信号: {}", symbol, result.getFinalSignal());
        }
        
        log.info("\n✓ 所有股票分析完成");
        log.info("  每个股票的报告已写入各自目录");
    }
    
    /**
     * 展示生成的报告结构
     */
    private void displayReportStructure() {
        log.info("\n" + "=".repeat(80));
        log.info("【生成的报告目录结构】");
        log.info("=".repeat(80));
        
        Path reportsDir = Paths.get("reports");
        
        if (!Files.exists(reportsDir)) {
            log.warn("报告目录不存在: {}", reportsDir.toAbsolutePath());
            return;
        }
        
        try {
            log.info("\n报告根目录: {}", reportsDir.toAbsolutePath());
            log.info("");
            
            // 遍历所有股票目录
            try (Stream<Path> stockDirs = Files.list(reportsDir)) {
                stockDirs.filter(Files::isDirectory).forEach(stockDir -> {
                    String symbol = stockDir.getFileName().toString();
                    log.info("📁 {}/", symbol);
                    
                    try (Stream<Path> dateDirs = Files.list(stockDir)) {
                        dateDirs.filter(Files::isDirectory).forEach(dateDir -> {
                            String date = dateDir.getFileName().toString();
                            log.info("  📅 {}/", date);
                            
                            try (Stream<Path> files = Files.list(dateDir)) {
                                files.filter(Files::isRegularFile)
                                     .sorted()
                                     .forEach(file -> {
                                         String fileName = file.getFileName().toString();
                                         long size = 0;
                                         try {
                                             size = Files.size(file);
                                         } catch (Exception e) {
                                             // ignore
                                         }
                                         
                                         String icon = getFileIcon(fileName);
                                         log.info("    {} {} ({} bytes)", icon, fileName, size);
                                     });
                            } catch (Exception e) {
                                log.error("读取文件列表失败", e);
                            }
                        });
                    } catch (Exception e) {
                        log.error("读取日期目录失败", e);
                    }
                    
                    log.info("");
                });
            }
            
            log.info("=".repeat(80));
            log.info("说明：");
            log.info("  📊 = 最终摘要报告（推荐首先查看）");
            log.info("  📈 = 分析师报告");
            log.info("  💬 = 研究员辩论");
            log.info("  ⚖️  = 风险辩论");
            log.info("  ✅ = 决策和计划");
            log.info("  🧠 = 反思记录");
            log.info("=".repeat(80));
            
        } catch (Exception e) {
            log.error("展示报告结构失败", e);
        }
    }
    
    private String getFileIcon(String fileName) {
        if (fileName.contains("FINAL_SUMMARY")) {
            return "📊";
        } else if (fileName.contains("analyst_reports")) {
            return "📈";
        } else if (fileName.contains("researcher_debate")) {
            return "💬";
        } else if (fileName.contains("risk_debate")) {
            return "⚖️";
        } else if (fileName.contains("decision") || fileName.contains("plan")) {
            return "✅";
        } else if (fileName.contains("reflections")) {
            return "🧠";
        } else {
            return "📄";
        }
    }
    
    private void printBanner() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  ____                       _     __        __    _ _            ");
        System.out.println(" |  _ \\ ___ _ __   ___  _ __| |_  \\ \\      / / __(_) |_ ___ _ __ ");
        System.out.println(" | |_) / _ \\ '_ \\ / _ \\| '__| __|  \\ \\ /\\ / / '__| | __/ _ \\ '__|");
        System.out.println(" |  _ <  __/ |_) | (_) | |  | |_    \\ V  V /| |  | | ||  __/ |   ");
        System.out.println(" |_| \\_\\___| .__/ \\___/|_|   \\__|    \\_/\\_/ |_|  |_|\\__\\___|_|   ");
        System.out.println("           |_|                                                    ");
        System.out.println();
        System.out.println("  JTrade 报告写入功能演示");
        System.out.println("  展示如何将分析结果写入特定目录");
        System.out.println("=".repeat(80));
        
        System.out.println("\n✨ 功能特性：");
        System.out.println("  1. 📁 按股票代码组织目录结构");
        System.out.println("  2. 📅 按日期分离不同时间的分析");
        System.out.println("  3. 📊 生成完整的最终摘要报告");
        System.out.println("  4. 📈 分别保存各阶段中间结果");
        System.out.println("  5. 🔍 易于查找和对比不同股票的分析");
        
        System.out.println("\n📂 目录结构示例：");
        System.out.println("  reports/");
        System.out.println("    ├── AAPL/");
        System.out.println("    │   └── 2025-10-30/");
        System.out.println("    │       ├── 20251030_220000_FINAL_SUMMARY.txt");
        System.out.println("    │       ├── 20251030_220000_analyst_reports.txt");
        System.out.println("    │       ├── 20251030_220000_researcher_debate.txt");
        System.out.println("    │       ├── 20251030_220000_trading_plan.txt");
        System.out.println("    │       └── ...");
        System.out.println("    ├── TSLA/");
        System.out.println("    │   └── 2025-10-30/");
        System.out.println("    └── MSFT/");
        System.out.println("        └── 2025-10-30/");
        System.out.println();
    }
}
