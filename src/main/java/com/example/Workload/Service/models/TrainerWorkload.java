package com.example.Workload.Service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
public class TrainerWorkload {

    @Id
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    private boolean trainerStatus;

    @ElementCollection
    @CollectionTable(name = "trainer_workload_hours")
    @MapKeyColumn(name = "year_month")
    @Column(name = "duration")
    private Map<YearMonth, Integer> workloadHours = new HashMap<>();

    public void addHours(YearMonth yearMonth, int hours) {
        if (hours <= 0) throw new IllegalArgumentException("Hours must be positive");
        workloadHours.merge(yearMonth, hours, Integer::sum);
    }

    public void removeHours(YearMonth yearMonth, int hours) {
        if (hours <= 0) throw new IllegalArgumentException("Hours must be positive");
        workloadHours.computeIfPresent(yearMonth, (k, v) -> Math.max(0, v - hours));
    }

    public int getHours(YearMonth yearMonth) {
        return workloadHours.getOrDefault(yearMonth, 0);
    }
}
