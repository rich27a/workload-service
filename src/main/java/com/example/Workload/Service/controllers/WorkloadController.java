package com.example.Workload.Service.controllers;


import com.example.Workload.Service.models.WorkloadRequest;
import com.example.Workload.Service.models.WorkloadSummary;
import com.example.Workload.Service.service.WorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Workload Management", description = "API endpoints for managing and retrieving trainer workload information")
public class WorkloadController {

    private final WorkloadService workloadService;

    @Operation(summary = "Process a new workload request",
            description = "Receives and processes workload information for a trainer")
    @PostMapping
    public ResponseEntity<?> processWorkload(@RequestBody WorkloadRequest request,
                                             @RequestHeader(value = "X-Transaction-ID", required = false) String transactionId) {
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        log.info("[Transaction: {}] Received workload request for trainer: {}",
                transactionId, request.trainerUsername());
        workloadService.processWorkload(request, transactionId);

        log.info("[Transaction: {}] Successfully processed workload for trainer: {}",
                transactionId, request.trainerUsername());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get monthly workload summary",
            description = "Retrieves the workload summary for a specific trainer for a given month")
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

