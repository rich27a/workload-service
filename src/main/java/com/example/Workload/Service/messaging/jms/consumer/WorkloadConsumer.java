package com.example.Workload.Service.messaging.jms.consumer;

import com.example.Workload.Service.constants.JmsConstants;
import com.example.Workload.Service.messaging.jms.model.WorkloadMessage;
import com.example.Workload.Service.service.WorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class WorkloadConsumer {
    private static final Logger log = LoggerFactory.getLogger(WorkloadConsumer.class);

    private final WorkloadService workloadService;


    public WorkloadConsumer(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @JmsListener(destination = "${workload-service.activemq.workload.queue}")
    public void receiveWorkloadMessage(WorkloadMessage message,
                                       @Header(JmsConstants.TRANSACTION_ID_PROPERTY) String transactionId) {
        log.info("[Transaction: {}] Received workload message for trainer: {} with action: {}",
                transactionId, message.getTrainerUsername(), message.getActionType());

        try {
            workloadService.processWorkload(message, transactionId);
            log.info("[Transaction: {}] Successfully processed workload message", transactionId);
        } catch (Exception e) {
            log.error("[Transaction: {}] Error processing workload message: {}",
                    transactionId, e.getMessage(), e);
            throw e;
        }
    }
}
