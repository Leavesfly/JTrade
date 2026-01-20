package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.core.prompt.PromptManager;
import io.leavesfly.jtrade.core.prompt.PromptManager.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

/**
 * Prompt 管理系统演示
 * 
 * 展示如何使用 PromptManager 管理和定制智能体提示词
 * 
 * 功能演示：
 * 1. 加载和查看所有 Prompt 模板
 * 2. 动态替换变量构建提示
 * 3. 比较不同智能体的提示风格
 * 4. 演示提示词的可定制性
 * 
 * @author 山泽
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "io.leavesfly.jtrade")
public class PromptManagementDemo {
    
    public static void main(String[] args) {
        SpringApplication.run(PromptManagementDemo.class, args);
    }
    
    @Bean
    public CommandLineRunner promptManagementDemoRunner(PromptManager promptManager) {
        return args -> {
            printBanner();
            
            // 1. 展示分析师 Prompts
            System.out.println("\n" + "=".repeat(80));
            System.out.println("【演示1】分析师团队 Prompt 模板");
            System.out.println("=".repeat(80));
            demonstrateAnalystPrompts(promptManager);
            
            // 2. 展示研究员 Prompts
            System.out.println("\n" + "=".repeat(80));
            System.out.println("【演示2】研究员团队 Prompt 模板");
            System.out.println("=".repeat(80));
            demonstrateResearcherPrompts(promptManager);
            
            // 3. 展示管理员 Prompts
            System.out.println("\n" + "=".repeat(80));
            System.out.println("【演示3】管理员团队 Prompt 模板");
            System.out.println("=".repeat(80));
            demonstrateManagerPrompts(promptManager);
            
            // 4. 展示反思 Prompts
            System.out.println("\n" + "=".repeat(80));
            System.out.println("【演示4】反思系统 Prompt 模板");
            System.out.println("=".repeat(80));
            demonstrateReflectionPrompts(promptManager);
            
            // 5. 演示动态变量替换
            System.out.println("\n" + "=".repeat(80));
            System.out.println("【演示5】动态变量替换");
            System.out.println("=".repeat(80));
            demonstrateVariableReplacement(promptManager);
            
            // 6. 演示提示词对比
            System.out.println("\n" + "=".repeat(80));
            System.out.println("【演示6】多空观点对比");
            System.out.println("=".repeat(80));
            demonstrateBullVsBear(promptManager);
            
            // 总结
            printSummary();
        };
    }
    
    private void demonstrateAnalystPrompts(PromptManager promptManager) {
        System.out.println("\n📊 市场分析师 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate marketPrompt = promptManager.getMarketAnalystPrompt();
        System.out.println("System: " + truncate(marketPrompt.getSystemPrompt(), 150));
        System.out.println("\nUser Template: " + truncate(marketPrompt.getUserPrompt(), 200));
        
        System.out.println("\n\n💼 基本面分析师 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate fundamentalsPrompt = promptManager.getFundamentalsAnalystPrompt();
        System.out.println("System: " + truncate(fundamentalsPrompt.getSystemPrompt(), 150));
        System.out.println("\nUser Template: " + truncate(fundamentalsPrompt.getUserPrompt(), 200));
        
        System.out.println("\n\n📰 新闻分析师 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate newsPrompt = promptManager.getNewsAnalystPrompt();
        System.out.println("System: " + truncate(newsPrompt.getSystemPrompt(), 150));
        
        System.out.println("\n\n📱 社交媒体分析师 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate socialPrompt = promptManager.getSocialMediaAnalystPrompt();
        System.out.println("System: " + truncate(socialPrompt.getSystemPrompt(), 150));
        
        System.out.println("\n✅ 分析师团队共4个成员，每个都有专门的提示词");
    }
    
    private void demonstrateResearcherPrompts(PromptManager promptManager) {
        System.out.println("\n📈 多头研究员 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate bullPrompt = promptManager.getBullResearcherPrompt();
        System.out.println("System: " + bullPrompt.getSystemPrompt());
        System.out.println("\nUser Template: " + truncate(bullPrompt.getUserPrompt(), 250));
        
        System.out.println("\n\n📉 空头研究员 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate bearPrompt = promptManager.getBearResearcherPrompt();
        System.out.println("System: " + bearPrompt.getSystemPrompt());
        System.out.println("\nUser Template: " + truncate(bearPrompt.getUserPrompt(), 250));
        
        System.out.println("\n✅ 研究员团队采用辩论机制，多空对立确保决策客观");
    }
    
    private void demonstrateManagerPrompts(PromptManager promptManager) {
        System.out.println("\n👔 研究经理 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate researchMgrPrompt = promptManager.getResearchManagerPrompt();
        System.out.println("System: " + researchMgrPrompt.getSystemPrompt());
        
        System.out.println("\n\n💰 交易员 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate traderPrompt = promptManager.getTraderPrompt();
        System.out.println("System: " + traderPrompt.getSystemPrompt());
        
        System.out.println("\n\n🛡️ 风险管理器 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate riskPrompt = promptManager.getRiskManagerPrompt();
        System.out.println("System: " + riskPrompt.getSystemPrompt());
        
        System.out.println("\n✅ 管理团队负责决策、执行和风控三个关键环节");
    }
    
    private void demonstrateReflectionPrompts(PromptManager promptManager) {
        System.out.println("\n🔍 分析反思 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate analysisReflection = promptManager.getAnalysisReflectionPrompt();
        System.out.println(analysisReflection.getSystemPrompt());
        
        System.out.println("\n\n⚠️ 决策反思 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate decisionReflection = promptManager.getDecisionReflectionPrompt();
        System.out.println(decisionReflection.getSystemPrompt());
        
        System.out.println("\n\n📋 综合反思 Prompt:");
        System.out.println("-".repeat(80));
        PromptTemplate comprehensiveReflection = promptManager.getComprehensiveReflectionPrompt();
        System.out.println(comprehensiveReflection.getSystemPrompt());
        
        System.out.println("\n✅ 三层反思机制确保决策质量和持续改进");
    }
    
    private void demonstrateVariableReplacement(PromptManager promptManager) {
        System.out.println("\n演示：为 AAPL 股票构建市场分析提示");
        System.out.println("-".repeat(80));
        
        PromptTemplate marketPrompt = promptManager.getMarketAnalystPrompt();
        
        Map<String, String> variables = new HashMap<>();
        variables.put("symbol", "AAPL");
        variables.put("date", "2024-01-15");
        variables.put("indicators", 
            "MA5: 185.23, MA20: 182.45\n" +
            "RSI: 62.5\n" +
            "MACD: 1.23 (Signal: 0.98)\n" +
            "Volume: 85.2M");
        
        String builtPrompt = marketPrompt.buildUserPrompt(variables);
        
        System.out.println("原始模板:");
        System.out.println(truncate(marketPrompt.getUserPrompt(), 200));
        System.out.println("\n↓↓↓ 替换变量后 ↓↓↓\n");
        System.out.println("构建的提示:");
        System.out.println(builtPrompt);
        
        System.out.println("\n✅ 变量替换功能使提示词可以动态适应不同股票和数据");
    }
    
    private void demonstrateBullVsBear(PromptManager promptManager) {
        System.out.println("\n对比多头和空头研究员的关注点差异：");
        System.out.println("-".repeat(80));
        
        PromptTemplate bullPrompt = promptManager.getBullResearcherPrompt();
        PromptTemplate bearPrompt = promptManager.getBearResearcherPrompt();
        
        System.out.println("📈 多头研究员关注：");
        System.out.println("  - 增长潜力和市场机会");
        System.out.println("  - 竞争优势和创新能力");
        System.out.println("  - 积极的财务指标");
        System.out.println("  - 正面新闻和市场情绪");
        System.out.println("  系统提示: " + bullPrompt.getSystemPrompt());
        
        System.out.println("\n📉 空头研究员关注：");
        System.out.println("  - 潜在风险和挑战");
        System.out.println("  - 竞争劣势和威胁");
        System.out.println("  - 负面财务指标");
        System.out.println("  - 不利新闻和负面情绪");
        System.out.println("  系统提示: " + bearPrompt.getSystemPrompt());
        
        System.out.println("\n💡 辩论机制确保决策过程考虑了正反两面的观点");
    }
    
    private void printBanner() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  ____                            _     __  __                                   ");
        System.out.println(" |  _ \\ _ __ ___  _ __ ___  _ __ | |_  |  \\/  | __ _ _ __   __ _  __ _  ___ _ __ ");
        System.out.println(" | |_) | '__/ _ \\| '_ ` _ \\| '_ \\| __| | |\\/| |/ _` | '_ \\ / _` |/ _` |/ _ \\ '__|");
        System.out.println(" |  __/| | | (_) | | | | | | |_) | |_  | |  | | (_| | | | | (_| | (_| |  __/ |   ");
        System.out.println(" |_|   |_|  \\___/|_| |_| |_| .__/ \\__| |_|  |_|\\__,_|_| |_|\\__,_|\\__, |\\___|_|   ");
        System.out.println("                           |_|                                    |___/            ");
        System.out.println();
        System.out.println("  JTrade Prompt 管理系统演示");
        System.out.println("  展示如何集中管理和定制所有智能体的提示词");
        System.out.println("=".repeat(80));
    }
    
    private void printSummary() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 Prompt 管理系统总结");
        System.out.println("=".repeat(80));
        
        System.out.println("\n✅ 已实现的功能：");
        System.out.println("  1. 集中式 Prompt 配置文件 (agent-prompts.properties)");
        System.out.println("  2. PromptManager 管理类，自动加载和缓存");
        System.out.println("  3. 为每个智能体提供专门的提示模板");
        System.out.println("  4. 支持动态变量替换");
        System.out.println("  5. 系统提示和用户提示分离");
        
        System.out.println("\n💡 优势：");
        System.out.println("  - 易于维护：所有提示集中管理，修改方便");
        System.out.println("  - 易于定制：可以根据不同场景调整提示词");
        System.out.println("  - 版本控制：提示词的变更可以追踪");
        System.out.println("  - 多语言支持：可以轻松添加不同语言的提示文件");
        System.out.println("  - A/B测试：可以测试不同提示词的效果");
        
        System.out.println("\n🎯 覆盖的智能体：");
        System.out.println("  - 4个分析师：市场、基本面、新闻、社交媒体");
        System.out.println("  - 2个研究员：多头、空头");
        System.out.println("  - 3个管理员：研究经理、交易员、风险管理器");
        System.out.println("  - 3个反思层：分析反思、决策反思、综合反思");
        
        System.out.println("\n📁 文件位置：");
        System.out.println("  - Prompt 配置: src/main/resources/prompts/agent-prompts.properties");
        System.out.println("  - 管理类: src/main/java/io/leavesfly/jtrade/core/prompt/PromptManager.java");
        
        System.out.println("\n🔧 使用示例：");
        System.out.println("  ```java");
        System.out.println("  @Autowired");
        System.out.println("  private PromptManager promptManager;");
        System.out.println("  ");
        System.out.println("  // 获取提示模板");
        System.out.println("  PromptTemplate template = promptManager.getMarketAnalystPrompt();");
        System.out.println("  ");
        System.out.println("  // 构建提示（替换变量）");
        System.out.println("  Map<String, String> vars = new HashMap<>();");
        System.out.println("  vars.put(\"symbol\", \"AAPL\");");
        System.out.println("  String prompt = template.buildUserPrompt(vars);");
        System.out.println("  ```");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ Prompt 管理系统演示完成！");
        System.out.println("=".repeat(80) + "\n");
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
