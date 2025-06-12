package com.example.Workload.Service.service;

import com.example.Workload.Service.models.documents.TrainerSummary;

@FunctionalInterface
interface WorkloadActionProcessor {
    void process(TrainerSummary trainerSummary, int year, int month, int duration, String transactionId);
}
