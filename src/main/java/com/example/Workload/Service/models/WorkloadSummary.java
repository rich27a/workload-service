package com.example.Workload.Service.models;
public record WorkloadSummary(
        String trainerUsername,
        int year,
        int month,
        int totalHours
) {}
