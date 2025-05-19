package com.example.Workload.Service.messaging.jms.model;



import com.example.Workload.Service.models.ActionType;
import com.example.Workload.Service.models.WorkloadData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadMessage implements Serializable, WorkloadData {
    private static final long serialVersionUID = 1L;

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean isActive;
    private LocalDate trainingDate;
    private int trainingDuration;
    private ActionType actionType;
    private String authToken;
}
