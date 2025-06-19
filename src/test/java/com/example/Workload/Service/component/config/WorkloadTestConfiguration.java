package com.example.Workload.Service.component.config;

import com.example.Workload.Service.WorkloadServiceApplication;
import com.example.Workload.Service.component.support.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;



@TestConfiguration
@Import(WorkloadServiceApplication.class)
public class WorkloadTestConfiguration {

    @Bean
    public TestDataHelper testDataHelper() {
        return new TestDataHelper();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
