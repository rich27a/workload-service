package com.example.Workload.Service.repositories.mongo;

import com.example.Workload.Service.models.documents.TrainerSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerSummaryRepository extends MongoRepository<TrainerSummary, String> {
    /**
     * Find trainer summary by username
     * @param username Trainer username
     * @return Optional TrainerSummary
     */
    Optional<TrainerSummary> findByTrainerUsername(String username);

    /**
     * Find trainer summaries by first name and last name
     * @param firstName Trainer first name
     * @param lastName Trainer last name
     * @return List of TrainerSummary
     */
    @Query("{'trainerFirstName': ?0, 'trainerLastName': ?1}")
    List<TrainerSummary> findByTrainerFirstNameAndTrainerLastName(String firstName, String lastName);
}
