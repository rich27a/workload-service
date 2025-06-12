package com.example.Workload.Service.service;

import com.example.Workload.Service.advice.TrainerNotFoundException;
import com.example.Workload.Service.messaging.jms.model.WorkloadMessage;
import com.example.Workload.Service.models.*;
import com.example.Workload.Service.models.documents.TrainerSummary;
import com.example.Workload.Service.repositories.TrainerWorkloadRepository;
import com.example.Workload.Service.repositories.mongo.TrainerSummaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class WorkloadService {
    private final TrainerSummaryRepository trainerSummaryRepository;
    public WorkloadService(TrainerSummaryRepository trainerSummaryRepository) {
        this.trainerSummaryRepository = trainerSummaryRepository;
    }

    private final Map<ActionType, WorkloadActionProcessor> actionProcessors = Map.of(
            ActionType.ADD, this::addTrainingDuration,
            ActionType.DELETE, this::removeTrainingDuration
    );
    @Transactional
    public void processWorkload(WorkloadData request, String transactionId) {
        log.info("[{}] Processing workload for trainer: {}",
                transactionId, request.getTrainerUsername());
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        log.debug("[Transaction: {}] Training date: year={}, month={}",
                transactionId, year, month);
        TrainerSummary trainerSummary = getOrCreateTrainerSummary(request, transactionId);
        actionProcessors.get(request.getActionType())
                .process(trainerSummary, year, month, request.getTrainingDuration(), transactionId);
        trainerSummaryRepository.save(trainerSummary);
        log.info("[{}] Workload saved for trainer: {}",
                transactionId, request.getTrainerUsername());
    }
    private void addTrainingDuration(TrainerSummary trainerSummary, int year, int month,
                                     int duration, String transactionId) {
        log.debug("[Transaction: {}] Adding {} hours to trainer: {} for {}-{}",
                transactionId, duration, trainerSummary.getTrainerUsername(), year, month);

        TrainerSummary.YearSummary yearSummary = trainerSummary.getOrCreateYearSummary(year);
        TrainerSummary.MonthSummary monthSummary = yearSummary.getOrCreateMonthSummary(month);

        int currentDuration = monthSummary.getTrainingsSummaryDuration() != null ?
                monthSummary.getTrainingsSummaryDuration().intValue() : 0;
        monthSummary.setTrainingsSummaryDuration(currentDuration + duration);
    }
    private void removeTrainingDuration(TrainerSummary trainerSummary, int year, int month,
                                        int duration, String transactionId) {
        log.debug("[Transaction: {}] Removing {} hours from trainer: {} for {}-{}",
                transactionId, duration, trainerSummary.getTrainerUsername(), year, month);

        TrainerSummary.YearSummary yearSummary = trainerSummary.getOrCreateYearSummary(year);
        TrainerSummary.MonthSummary monthSummary = yearSummary.getOrCreateMonthSummary(month);

        int currentDuration = monthSummary.getTrainingsSummaryDuration() != null ?
                monthSummary.getTrainingsSummaryDuration().intValue() : 0;
        int newDuration = Math.max(0, currentDuration - duration);
        monthSummary.setTrainingsSummaryDuration(newDuration);
    }
    @Transactional
    public TrainerSummary getWorkloadSummary(String username,
                                              YearMonth yearMonth,
                                              String transactionId) {
        log.info("[{}] Getting summary for {} in {}",
                transactionId, username, yearMonth);
        return trainerSummaryRepository.findById(username)
                .orElseThrow(() -> new TrainerNotFoundException(username));
    }
    private TrainerSummary getOrCreateTrainerSummary(WorkloadData workloadData, String transactionId) {
        return trainerSummaryRepository.findByTrainerUsername(workloadData.getTrainerUsername())
                .orElseGet(() -> {
                    log.debug("[Transaction: {}] Creating new trainer summary for: {}",
                            transactionId, workloadData.getTrainerUsername());
                    TrainerSummary newSummary = new TrainerSummary();
                    newSummary.setTrainerUsername(workloadData.getTrainerUsername());
                    newSummary.setTrainerFirstName(workloadData.getTrainerFirstName());
                    newSummary.setTrainerLastName(workloadData.getTrainerLastName());
                    newSummary.setTrainerStatus(workloadData.isActive());
                    return newSummary;
                });
    }

}
