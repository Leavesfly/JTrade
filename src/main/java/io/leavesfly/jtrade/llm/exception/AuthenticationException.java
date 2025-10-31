package io.leavesfly.jtrade.llm.exception;

/**
 * 认证异常
 * 
 * @author 山泽
 */
public class AuthenticationException extends LlmException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
