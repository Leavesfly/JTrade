package io.leavesfly.jtrade.demo;

import io.leavesfly.jtrade.core.prompt.PromptManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agent 模板化提示迁移演示
 * 
 * 展示所有 Agent 如何使用 PromptManager 管理的模板化提示
 * 
 * @author 山泽
 */
public class TemplateMigrationDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Agent 模板化提示迁移完成演示 ===\n");
        
        PromptManager promptManager = new PromptManager();
        
        // 1. 迁移概览
        System.out.println("【迁移概览】");
        System.out.println("已完成 12 个 Agent 的模板化提示迁移：\n");
        
        String[][] agents = {
            {"分析师团队", "4个"},
            {"  - MarketAnalyst", "react.analyst.market"},
            {"  - FundamentalsAnalyst", "react.analyst.fundamentals"},
            {"  - NewsAnalyst", "react.analyst.news"},
            {"  - SocialMediaAnalyst", "react.analyst.social"},
            {"", ""},
            {"研究员团队", "2个"},
            {"  - BullResearcher", "react.researcher.bull"},
            {"  - BearResearcher", "react.researcher.bear"},
            {"", ""},
            {"交易与管理", "3个"},
            {"  - Trader", "react.trader"},
            {"  - ResearchManager", "manager.research (传统格式)"},
            {"  - RiskManager", "react.manager.risk"},
            {"", ""},
            {"风险辩论者", "3个"},
            {"  - AggressiveDebator", "react.debator.aggressive"},
            {"  - ConservativeDebator", "react.debator.conservative"},
            {"  - NeutralDebator", "react.debator.neutral"}
        };
        
        for (String[] agent : agents) {
            if (agent[0].isEmpty()) {
                System.out.println();
            } else if (!agent[0].startsWith("  ")) {
                System.out.printf("%-20s %s\n", agent[0], agent[1]);
            } else {
                System.out.printf("%-40s → %s\n", agent[0], agent[1]);
            }
        }
        
        // 2. 模板示例
        System.out.println("\n【模板示例】以 MarketAnalyst 为例：\n");
        
        String marketSystemPrompt = promptManager.getSystemPrompt("react.analyst.market");
        String marketUserPrompt = promptManager.getUserPrompt("react.analyst.market");
        
        System.out.println("系统提示（前200字符）：");
        System.out.println(truncate(marketSystemPrompt, 200));
        System.out.println("\n用户提示模板（前200字符）：");
        System.out.println(truncate(marketUserPrompt, 200));
        
        // 3. 变量替换演示
        System.out.println("\n【变量替换演示】\n");
        
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("symbol", "TSLA");
        variables.put("date", "2025-10-31");
        variables.put("tools", "- fundamentals\n- market_indicators\n- news");
        
        String filledPrompt = promptManager.buildPrompt(marketUserPrompt, variables);
        System.out.println("替换变量后的用户提示（前300字符）：");
        System.out.println(truncate(filledPrompt, 300));
        
        // 4. 降级机制说明
        System.out.println("\n【降级机制】\n");
        System.out.println("如果 Agent 构造时未注入 PromptManager，或模板键名返回 null：");
        System.out.println("  → BaseRecAgent 会自动使用硬编码的默认提示");
        System.out.println("  → 保证系统兼容性，不会因缺少配置而失败\n");
        
        // 5. 使用方法
        System.out.println("【Agent 端使用方法】\n");
        System.out.println("每个 Agent 只需覆盖 getPromptKey() 方法：");
        System.out.println("```java");
        System.out.println("@Override");
        System.out.println("protected String getPromptKey() {");
        System.out.println("    return \"react.analyst.market\";  // 返回对应的模板键名");
        System.out.println("}");
        System.out.println("```\n");
        
        System.out.println("BaseRecAgent 会自动：");
        System.out.println("  1. 从 PromptManager 加载系统提示和用户提示模板");
        System.out.println("  2. 替换模板中的变量（{symbol}, {date}, {tools} 等）");
        System.out.println("  3. 如果加载失败，降级使用硬编码提示\n");
        
        // 6. 优化效果
        System.out.println("【优化效果总结】\n");
        System.out.println("✓ 提示词集中管理：所有提示词在 agent-prompts.properties 统一维护");
        System.out.println("✓ 热更新支持：修改配置文件后重启即可生效，无需重新编译");
        System.out.println("✓ 版本控制友好：提示词变更可通过 Git 清晰追踪");
        System.out.println("✓ A/B 测试便利：可快速切换不同版本的提示词进行实验");
        System.out.println("✓ 降级机制保障：确保系统在缺少配置时仍能正常运行");
        System.out.println("✓ 变量注入灵活：支持动态替换上下文变量，适应不同场景");
        System.out.println("✓ 教育价值高：清晰展示配置管理最佳实践\n");
        
        // 7. 配置文件位置
        System.out.println("【配置文件位置】");
        System.out.println("src/main/resources/prompts/agent-prompts.properties\n");
        
        // 8. 迁移状态统计
        System.out.println("【迁移状态统计】");
        System.out.println("总 Agent 数量：12");
        System.out.println("已迁移数量：12 (100%)");
        System.out.println("ReAct 模式：11 个");
        System.out.println("传统模式：1 个 (ResearchManager)");
        System.out.println("迁移状态：✅ 全部完成\n");
        
        System.out.println("=== 演示完成 ===");
    }
    
    private static String truncate(String text, int maxLength) {
        if (text == null) return "null";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
