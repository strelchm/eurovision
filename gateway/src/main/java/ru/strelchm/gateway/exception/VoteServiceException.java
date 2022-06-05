package ru.strelchm.gateway.exception;

public class VoteServiceException extends RuntimeException {
    public VoteServiceException(String message) {
        super(message);
    }
}
