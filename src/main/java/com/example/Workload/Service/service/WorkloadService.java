package com.example.Workload.Service.service;

import com.example.Workload.Service.advice.TrainerNotFoundException;
import com.example.Workload.Service.messaging.jms.model.WorkloadMessage;
import com.example.Workload.Service.models.*;
import com.example.Workload.Service.repositories.TrainerWorkloadRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class WorkloadService {

    private final TrainerWorkloadRepository workloadRepository;
    private final Map<ActionType, WorkloadActionHandler> actionHandlers = Map.of(
            ActionType.ADD, this::handleAddAction,
            ActionType.DELETE, this::handleDeleteAction
    );

    public void processWorkload(WorkloadMessage request, String transactionId) {
        log.info("[{}] Processing workload for trainer: {}",
                transactionId, request.getTrainerUsername());

        TrainerWorkload trainerWorkload = getOrCreateWorkload(request);
        YearMonth yearMonth = YearMonth.from(request.getTrainingDate());

        actionHandlers.get(request.getActionType())
                .handle(request, trainerWorkload, yearMonth);

        workloadRepository.save(trainerWorkload);
        log.info("[{}] Workload saved for trainer: {}",
                transactionId, request.getTrainerUsername());
    }

    private void handleAddAction(WorkloadMessage request,
                                 TrainerWorkload workload,
                                 YearMonth yearMonth) {
        workload.addHours(yearMonth, request.getTrainingDuration());
    }

    private void handleDeleteAction(WorkloadMessage request,
                                    TrainerWorkload workload,
                                    YearMonth yearMonth) {
        workload.removeHours(yearMonth, request.getTrainingDuration());
    }

    @Transactional
    public WorkloadSummary getWorkloadSummary(String username,
                                              YearMonth yearMonth,
                                              String transactionId) {
        log.info("[{}] Getting summary for {} in {}",
                transactionId, username, yearMonth);

        return workloadRepository.findById(username)
                .map(w -> new WorkloadSummary(
                        w.getTrainerUsername(),
                        yearMonth.getYear(),
                        yearMonth.getMonthValue(),
                        w.getHours(yearMonth)))
                .orElseThrow(() -> new TrainerNotFoundException(username));
    }

    private TrainerWorkload getOrCreateWorkload(WorkloadMessage request) {
        return workloadRepository.findById(request.getTrainerUsername())
                .orElseGet(() -> {
                    TrainerWorkload newWorkload = new TrainerWorkload();
                    newWorkload.setTrainerUsername(request.getTrainerUsername());
                    newWorkload.setTrainerFirstName(request.getTrainerFirstName());
                    newWorkload.setTrainerLastName(request.getTrainerLastName());
                    newWorkload.setTrainerStatus(request.isActive());
                    newWorkload.setWorkloadHours(new HashMap<>());

                    log.debug("Creating new workload record for trainer: {}",
                            request.getTrainerUsername());
                    return newWorkload;
                });
    }
}
