package com.example.Workload.Service.controllers;


import com.example.Workload.Service.models.WorkloadRequest;
import com.example.Workload.Service.models.WorkloadSummary;
import com.example.Workload.Service.service.WorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Process a new workload request",
            description = "Receives and processes workload information for a trainer. The endpoint accepts a workload request containing trainer details and their workload information.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Workload request details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WorkloadRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Workload request processed successfully",
                    headers = @Header(name = "X-Transaction-ID", description = "Transaction ID for tracking the request", schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
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

    @Operation(
            summary = "Get monthly workload summary",
            description = "Retrieves the workload summary for a specific trainer for a given month. The summary includes aggregated workload information.",
            parameters = {
                    @Parameter(name = "username", description = "Username of the trainer", required = true, example = "john.doe"),
                    @Parameter(name = "year", description = "Year for the workload summary", required = true, example = "2024"),
                    @Parameter(name = "month", description = "Month for the workload summary (1-12)", required = true, example = "3")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved workload summary",
                    content = @Content(schema = @Schema(implementation = WorkloadSummary.class)),
                    headers = @Header(name = "X-Transaction-ID", description = "Transaction ID for tracking the request", schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trainer not found or no workload data available for the specified period",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid year/month parameters",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
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

