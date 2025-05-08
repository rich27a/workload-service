package com.example.Workload.Service.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkloadRequest {
    @NotBlank private String trainerUsername;
    @NotBlank private String trainerFirstName;
    @NotBlank private String trainerLastName;
    private boolean isActive;
    @NotNull private LocalDate trainingDate;
    @Positive private int trainingDuration;
    @NotNull private ActionType actionType;
}

