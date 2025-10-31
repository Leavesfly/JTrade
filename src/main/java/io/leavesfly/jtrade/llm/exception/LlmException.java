package io.leavesfly.jtrade.llm.exception;

/**
 * LLM基础异常类
 * 
 * @author 山泽
 */
public class LlmException extends RuntimeException {
    
    public LlmException(String message) {
        super(message);
    }
    
    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
