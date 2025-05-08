package com.example.Workload.Service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadSummary {
    private String trainerUsername;
    private int year;
    private int month;
    private int totalHours;
}
