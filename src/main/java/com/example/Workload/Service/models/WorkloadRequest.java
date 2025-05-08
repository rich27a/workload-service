package com.example.Workload.Service.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record WorkloadRequest(
        @NotBlank String trainerUsername,
        @NotBlank String trainerFirstName,
        @NotBlank String trainerLastName,
        boolean isActive,
        @NotNull LocalDate trainingDate,
        @Positive int trainingDuration,
        @NotNull ActionType actionType
) implements WorkloadData {
    @Override
    public String getTrainerUsername() {
        return trainerUsername;
    }

    @Override
    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    @Override
    public String getTrainerLastName() {
        return trainerLastName;
    }

    @Override
    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    @Override
    public int getTrainingDuration() {
        return trainingDuration;
    }

    @Override
    public ActionType getActionType() {
        return actionType;
    }
}

