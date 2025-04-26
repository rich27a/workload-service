package com.example.Workload.Service.advice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WorkloadExceptionHandler {

    @ExceptionHandler(TrainerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTrainerNotFound(TrainerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("TRAINER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(InvalidWorkloadRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidWorkloadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_REQUEST", ex.getMessage()));
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ErrorResponse {
    private String code;
    private String message;
}
