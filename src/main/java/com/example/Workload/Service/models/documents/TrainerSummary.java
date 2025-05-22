package com.example.Workload.Service.models.documents;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_summaries")
@Data
@NoArgsConstructor
@CompoundIndex(name = "name_index", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
public class TrainerSummary {

    @Id
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    private Boolean trainerStatus;

    private List<YearSummary> years = new ArrayList<>();

    public YearSummary getOrCreateYearSummary(int year) {
        for (YearSummary yearSummary : years) {
            if (yearSummary.getYear() == year) {
                return yearSummary;
            }
        }

        YearSummary newYearSummary = new YearSummary(year, new ArrayList<>());
        years.add(newYearSummary);
        return newYearSummary;
    }

    @Data
    @NoArgsConstructor
    public static class YearSummary {
        private int year;
        private List<MonthSummary> months = new ArrayList<>();

        public YearSummary(int year, List<MonthSummary> months) {
            this.year = year;
            this.months = months;
        }

        public MonthSummary getOrCreateMonthSummary(int month) {
            for (MonthSummary monthSummary : months) {
                if (monthSummary.getMonth() == month) {
                    return monthSummary;
                }
            }

            MonthSummary newMonthSummary = new MonthSummary(month, 0);
            months.add(newMonthSummary);
            return newMonthSummary;
        }
    }

    @Data
    @NoArgsConstructor
    public static class MonthSummary {
        private int month;
        private Number trainingsSummaryDuration;

        public MonthSummary(int month, Number trainingsSummaryDuration) {
            this.month = month;
            this.trainingsSummaryDuration = trainingsSummaryDuration;
        }
    }
}