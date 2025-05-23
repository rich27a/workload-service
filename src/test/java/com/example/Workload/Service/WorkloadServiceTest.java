package com.example.Workload.Service;

import com.example.Workload.Service.advice.TrainerNotFoundException;
import com.example.Workload.Service.models.ActionType;
import com.example.Workload.Service.models.WorkloadRequest;
import com.example.Workload.Service.models.documents.TrainerSummary;
import com.example.Workload.Service.repositories.mongo.TrainerSummaryRepository;
import com.example.Workload.Service.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock
    private TrainerSummaryRepository trainerSummaryRepository;

    @InjectMocks
    private WorkloadService workloadService;

    private WorkloadRequest workloadRequest;
    private TrainerSummary trainerSummary;
    private String transactionId;

    @BeforeEach
    void setUp() {
        transactionId = "test-transaction-123";

        workloadRequest = new WorkloadRequest(
                "john.doe",
                "John",
                "Doe",
                true,
                LocalDate.of(2024, 3, 15),
                60,
                ActionType.ADD
        );

        trainerSummary = new TrainerSummary();
        trainerSummary.setTrainerUsername("john.doe");
        trainerSummary.setTrainerFirstName("John");
        trainerSummary.setTrainerLastName("Doe");
        trainerSummary.setTrainerStatus(true);
        trainerSummary.setYears(new ArrayList<>());
    }

    @Test
    void processWorkload_AddAction_NewTrainer_ShouldCreateAndSaveTrainerSummary() {
        when(trainerSummaryRepository.findByTrainerUsername("john.doe"))
                .thenReturn(Optional.empty());
        when(trainerSummaryRepository.save(any(TrainerSummary.class)))
                .thenReturn(trainerSummary);
        
        workloadService.processWorkload(workloadRequest, transactionId);

        verify(trainerSummaryRepository).findByTrainerUsername("john.doe");
        verify(trainerSummaryRepository).save(argThat(summary -> {
            assertEquals("john.doe", summary.getTrainerUsername());
            assertEquals("John", summary.getTrainerFirstName());
            assertEquals("Doe", summary.getTrainerLastName());
            assertTrue(summary.getTrainerStatus());

            assertEquals(1, summary.getYears().size());
            TrainerSummary.YearSummary yearSummary = summary.getYears().get(0);
            assertEquals(2024, yearSummary.getYear());
            assertEquals(1, yearSummary.getMonths().size());

            TrainerSummary.MonthSummary monthSummary = yearSummary.getMonths().get(0);
            assertEquals(3, monthSummary.getMonth());
            assertEquals(60, monthSummary.getTrainingsSummaryDuration().intValue());

            return true;
        }));
    }

    @Test
    void processWorkload_AddAction_ExistingTrainer_ShouldUpdateDuration() {
        TrainerSummary.YearSummary yearSummary = new TrainerSummary.YearSummary(2024, new ArrayList<>());
        TrainerSummary.MonthSummary monthSummary = new TrainerSummary.MonthSummary(3, 30);
        yearSummary.getMonths().add(monthSummary);
        trainerSummary.getYears().add(yearSummary);

        when(trainerSummaryRepository.findByTrainerUsername("john.doe"))
                .thenReturn(Optional.of(trainerSummary));

        workloadService.processWorkload(workloadRequest, transactionId);

        verify(trainerSummaryRepository).save(argThat(summary -> {
            assertEquals(90, summary.getYears().get(0).getMonths().get(0)
                    .getTrainingsSummaryDuration().intValue());
            return true;
        }));
    }

    @Test
    void processWorkload_DeleteAction_ShouldReduceDuration() {
        WorkloadRequest deleteRequest = new WorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2024, 3, 15), 30, ActionType.DELETE
        );

        TrainerSummary.YearSummary yearSummary = new TrainerSummary.YearSummary(2024, new ArrayList<>());
        TrainerSummary.MonthSummary monthSummary = new TrainerSummary.MonthSummary(3, 60);
        yearSummary.getMonths().add(monthSummary);
        trainerSummary.getYears().add(yearSummary);

        when(trainerSummaryRepository.findByTrainerUsername("john.doe"))
                .thenReturn(Optional.of(trainerSummary));

        workloadService.processWorkload(deleteRequest, transactionId);

        verify(trainerSummaryRepository).save(argThat(summary -> {
            assertEquals(30, summary.getYears().get(0).getMonths().get(0)
                    .getTrainingsSummaryDuration().intValue());
            return true;
        }));
    }

    @Test
    void processWorkload_DeleteAction_DurationBelowZero_ShouldSetToZero() {
        WorkloadRequest deleteRequest = new WorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2024, 3, 15), 100, ActionType.DELETE
        );

        TrainerSummary.YearSummary yearSummary = new TrainerSummary.YearSummary(2024, new ArrayList<>());
        TrainerSummary.MonthSummary monthSummary = new TrainerSummary.MonthSummary(3, 50);
        yearSummary.getMonths().add(monthSummary);
        trainerSummary.getYears().add(yearSummary);

        when(trainerSummaryRepository.findByTrainerUsername("john.doe"))
                .thenReturn(Optional.of(trainerSummary));

        workloadService.processWorkload(deleteRequest, transactionId);

        verify(trainerSummaryRepository).save(argThat(summary -> {
            assertEquals(0, summary.getYears().get(0).getMonths().get(0)
                    .getTrainingsSummaryDuration().intValue());
            return true;
        }));
    }

    @Test
    void getWorkloadSummary_ExistingTrainer_ShouldReturnTrainerSummary() {
        YearMonth yearMonth = YearMonth.of(2024, 3);
        when(trainerSummaryRepository.findById("john.doe"))
                .thenReturn(Optional.of(trainerSummary));

        TrainerSummary result = workloadService.getWorkloadSummary("john.doe", yearMonth, transactionId);

        assertNotNull(result);
        assertEquals("john.doe", result.getTrainerUsername());
        assertEquals("John", result.getTrainerFirstName());
        assertEquals("Doe", result.getTrainerLastName());
        verify(trainerSummaryRepository).findById("john.doe");
    }

    @Test
    void getWorkloadSummary_NonExistentTrainer_ShouldThrowTrainerNotFoundException() {
        YearMonth yearMonth = YearMonth.of(2024, 3);
        when(trainerSummaryRepository.findById("nonexistent"))
                .thenReturn(Optional.empty());

        TrainerNotFoundException exception = assertThrows(
                TrainerNotFoundException.class,
                () -> workloadService.getWorkloadSummary("nonexistent", yearMonth, transactionId)
        );

        assertEquals("nonexistent", exception.getMessage());
        verify(trainerSummaryRepository).findById("nonexistent");
    }

    @Test
    void processWorkload_NewYearAndMonth_ShouldCreateNewYearAndMonthSummary() {
        when(trainerSummaryRepository.findByTrainerUsername("john.doe"))
                .thenReturn(Optional.of(trainerSummary));

        workloadService.processWorkload(workloadRequest, transactionId);

        verify(trainerSummaryRepository).save(argThat(summary -> {
            assertEquals(1, summary.getYears().size());
            TrainerSummary.YearSummary yearSummary = summary.getYears().get(0);
            assertEquals(2024, yearSummary.getYear());
            assertEquals(1, yearSummary.getMonths().size());

            TrainerSummary.MonthSummary monthSummary = yearSummary.getMonths().get(0);
            assertEquals(3, monthSummary.getMonth());
            assertEquals(60, monthSummary.getTrainingsSummaryDuration().intValue());

            return true;
        }));
    }
}