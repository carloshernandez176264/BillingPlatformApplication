package com.billingplatformapplication.shared.exception;

// ============================================================
// BusinessException - reglas de negocio violadas (422)
// ============================================================
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
