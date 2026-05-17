package com.billingplatformapplication.shared.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
    public DuplicateResourceException(String entity, String field, Object value) {
        super(entity + " already exists with " + field + ": " + value);
    }
}
