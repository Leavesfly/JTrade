package io.leavesfly.jtrade.core.reflection;

import io.leavesfly.jtrade.core.state.AgentState;
import io.leavesfly.jtrade.llm.client.LlmClient;
import io.leavesfly.jtrade.llm.model.LlmMessage;
import io.leavesfly.jtrade.llm.model.LlmResponse;
import io.leavesfly.jtrade.llm.model.ModelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 反思服务
 * 
 * 对分析结果进行反思和自我批评，提高决策质量
 * 
 * @author 山泽
 */
@Slf4j
@Service
public class ReflectionService {
    
    private final LlmClient llmClient;
    private final ModelConfig reflectionConfig;
    
    public ReflectionService(LlmClient llmClient) {
        this.llmClient = llmClient;
        this.reflectionConfig = ModelConfig.builder()
                .temperature(0.6)
                .maxTokens(1500)
                .build();
    }
    
    /**
     * 对分析师报告进行反思
     */
    public AgentState reflectOnAnalysis(AgentState state) {
        log.info("开始对分析结果进行反思");
        
        if (state.getAnalystReports().isEmpty()) {
            log.warn("没有分析报告，跳过反思");
            return state;
        }
        
        try {
            String allReports = String.join("\n\n", state.getAnalystReports());
            String prompt = buildAnalysisReflectionPrompt(state.getCompany(), allReports);
            
            List<LlmMessage> messages = new ArrayList<>();
            messages.add(LlmMessage.system(
                "你是一位经验丰富的分析审查专家，擅长发现分析中的盲点、偏见和不足之处。"
            ));
            messages.add(LlmMessage.user(prompt));
            
            LlmResponse response = llmClient.chat(messages, reflectionConfig);
            String reflection = response.getContent();
            
            log.info("分析反思完成，反思长度: {} 字符", reflection.length());
            
            return state.addReflection("【分析反思】\n" + reflection);
            
        } catch (Exception e) {
            log.error("分析反思失败", e);
            return state;
        }
    }
    
    /**
     * 对交易决策进行反思
     */
    public AgentState reflectOnDecision(AgentState state) {
        log.info("开始对交易决策进行反思");
        
        if (state.getResearchManagerDecision() == null) {
            log.warn("没有交易决策，跳过反思");
            return state;
        }
        
        try {
            String prompt = buildDecisionReflectionPrompt(
                state.getCompany(),
                state.getResearchManagerDecision(),
                state.getTradingPlan()
            );
            
            List<LlmMessage> messages = new ArrayList<>();
            messages.add(LlmMessage.system(
                "你是一位风险管理专家，擅长审视交易决策中的潜在风险和问题。"
            ));
            messages.add(LlmMessage.user(prompt));
            
            LlmResponse response = llmClient.chat(messages, reflectionConfig);
            String reflection = response.getContent();
            
            log.info("决策反思完成，反思长度: {} 字符", reflection.length());
            
            return state.addReflection("【决策反思】\n" + reflection);
            
        } catch (Exception e) {
            log.error("决策反思失败", e);
            return state;
        }
    }
    
    /**
     * 综合反思
     */
    public AgentState comprehensiveReflection(AgentState state) {
        log.info("开始综合反思");
        
        try {
            // 汇总所有信息
            StringBuilder context = new StringBuilder();
            context.append("股票代码: ").append(state.getCompany()).append("\n\n");
            
            if (!state.getAnalystReports().isEmpty()) {
                context.append("分析师报告:\n");
                state.getAnalystReports().forEach(r -> context.append(r).append("\n\n"));
            }
            
            if (!state.getResearcherViewpoints().isEmpty()) {
                context.append("研究员观点:\n");
                state.getResearcherViewpoints().forEach(v -> context.append(v).append("\n\n"));
            }
            
            if (state.getResearchManagerDecision() != null) {
                context.append("研究经理决策:\n").append(state.getResearchManagerDecision()).append("\n\n");
            }
            
            if (state.getTradingPlan() != null) {
                context.append("交易计划:\n").append(state.getTradingPlan()).append("\n\n");
            }
            
            if (state.getFinalSignal() != null) {
                context.append("最终信号: ").append(state.getFinalSignal()).append("\n\n");
            }
            
            String prompt = buildComprehensiveReflectionPrompt(context.toString());
            
            List<LlmMessage> messages = new ArrayList<>();
            messages.add(LlmMessage.system(
                "你是一位资深的投资顾问，负责对整个决策流程进行最终审视和总结。"
            ));
            messages.add(LlmMessage.user(prompt));
            
            LlmResponse response = llmClient.chat(messages, reflectionConfig);
            String reflection = response.getContent();
            
            log.info("综合反思完成，反思长度: {} 字符", reflection.length());
            
            return state.addReflection("【综合反思】\n" + reflection);
            
        } catch (Exception e) {
            log.error("综合反思失败", e);
            return state;
        }
    }
    
    private String buildAnalysisReflectionPrompt(String symbol, String reports) {
        return String.format(
            "请审视以下对 %s 的分析报告，识别潜在的问题：\n\n" +
            "%s\n\n" +
            "请从以下角度进行反思：\n" +
            "1. 分析是否存在明显偏见或盲点？\n" +
            "2. 关键数据或因素是否被忽略？\n" +
            "3. 分析结论之间是否存在矛盾？\n" +
            "4. 风险评估是否充分？\n" +
            "5. 有哪些改进建议？",
            symbol, reports
        );
    }
    
    private String buildDecisionReflectionPrompt(String symbol, String decision, String plan) {
        return String.format(
            "请审视以下对 %s 的交易决策：\n\n" +
            "决策:\n%s\n\n" +
            "计划:\n%s\n\n" +
            "请从风险管理角度进行反思：\n" +
            "1. 决策是否考虑了极端情况？\n" +
            "2. 止损和止盈设置是否合理？\n" +
            "3. 仓位大小是否适当？\n" +
            "4. 是否有遗漏的风险因素？\n" +
            "5. 有哪些改进建议？",
            symbol, decision, plan != null ? plan : "未制定"
        );
    }
    
    private String buildComprehensiveReflectionPrompt(String fullContext) {
        return String.format(
            "请对以下完整的交易决策流程进行综合反思：\n\n" +
            "%s\n\n" +
            "请提供：\n" +
            "1. 整体流程的优点和不足\n" +
            "2. 决策质量评估\n" +
            "3. 潜在风险提示\n" +
            "4. 改进建议\n" +
            "5. 信心度评估（1-10分）",
            fullContext
        );
    }
}
