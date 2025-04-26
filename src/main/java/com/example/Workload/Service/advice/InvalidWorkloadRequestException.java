package com.example.Workload.Service.advice;

public class InvalidWorkloadRequestException extends RuntimeException {
    public InvalidWorkloadRequestException(String message) {
        super(message);
    }

    public InvalidWorkloadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
