package com.example.Workload.Service.service;

import com.example.Workload.Service.messaging.jms.model.WorkloadMessage;
import com.example.Workload.Service.models.TrainerWorkload;
import com.example.Workload.Service.models.WorkloadData;
import com.example.Workload.Service.models.WorkloadRequest;

import java.time.YearMonth;

@FunctionalInterface
interface WorkloadActionHandler {
    void handle(WorkloadData request, TrainerWorkload workload, YearMonth yearMonth);
}
