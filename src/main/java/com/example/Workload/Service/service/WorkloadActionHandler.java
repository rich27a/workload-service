package com.example.Workload.Service.service;

import com.example.Workload.Service.models.TrainerWorkload;
import com.example.Workload.Service.models.WorkloadRequest;

import java.time.YearMonth;

@FunctionalInterface
interface WorkloadActionHandler {
    void handle(WorkloadRequest request, TrainerWorkload workload, YearMonth yearMonth);
}
