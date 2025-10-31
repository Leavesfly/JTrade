package io.leavesfly.jtrade.llm.exception;

/**
 * 限流异常
 * 
 * @author 山泽
 */
public class RateLimitException extends LlmException {
    
    public RateLimitException(String message) {
        super(message);
    }
    
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
