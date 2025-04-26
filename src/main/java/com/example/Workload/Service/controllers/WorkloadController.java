package com.example.Workload.Service.controllers;


import com.example.Workload.Service.models.WorkloadRequest;
import com.example.Workload.Service.models.WorkloadSummary;
import com.example.Workload.Service.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
@Slf4j
public class WorkloadController {

    private final WorkloadService workloadService;

    @PostMapping
    public ResponseEntity<?> processWorkload(@RequestBody WorkloadRequest request,
                                             @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId) {
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        log.info("[Transaction: {}] Received workload request for trainer: {}",
                transactionId, request.getTrainerUsername());

        workloadService.processWorkload(request, transactionId);

        log.info("[Transaction: {}] Successfully processed workload for trainer: {}",
                transactionId, request.getTrainerUsername());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/{year}/{month}")
    public ResponseEntity<WorkloadSummary> getWorkloadSummary(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month,
            @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId) {

        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        log.info("[Transaction: {}] Received request for workload summary: trainer={}, year={}, month={}",
                transactionId, username, year, month);

        WorkloadSummary summary = workloadService.getWorkloadSummary(username, YearMonth.of(year, month), transactionId);

        log.info("[Transaction: {}] Returning workload summary for trainer: {}",
                transactionId, username);

        return ResponseEntity.ok(summary);
    }
}

