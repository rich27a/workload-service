package com.example.Workload.Service.repositories;

import com.example.Workload.Service.models.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, String> {
}
