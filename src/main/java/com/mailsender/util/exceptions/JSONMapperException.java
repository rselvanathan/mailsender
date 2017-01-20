package com.mailsender.util.exceptions;

/**
 * Represents an exception thrown by {@link com.mailsender.util.JSONMapper}
 */
public class JSONMapperException extends RuntimeException {
    public JSONMapperException(Exception e) {
        super(e);
    }
}
