Feature: Workload Management
  As a system administrator
  I want to manage trainer workloads
  So that I can track training hours and generate summaries

  Background:
    Given the workload service is running
    And I have a valid JWT token

  Scenario: Add training hours for a new trainer
    Given I have a workload request for trainer "john.doe"
      | trainerUsername   | john.doe        |
      | trainerFirstName  | John            |
      | trainerLastName   | Doe             |
      | isActive          | true            |
      | trainingDate      | 2024-03-15      |
      | trainingDuration  | 120             |
      | actionType        | ADD             |
    When I submit the workload request
    Then the workload should be processed successfully
    And the response status should be 200
    And the trainer summary should be created in the database

  Scenario: Add training hours for an existing trainer
    Given trainer "jane.smith" already exists in the system
      | trainerUsername   | jane.smith      |
      | trainerFirstName  | Jane            |
      | trainerLastName   | Smith           |
      | trainerStatus     | true            |
    And trainer "jane.smith" has 100 hours in March 2024
    When I add 60 hours for trainer "jane.smith" on "2024-03-20"
    Then the trainer should have 160 total hours in March 2024

  Scenario: Remove training hours from trainer workload
    Given trainer "bob.wilson" exists with the following workload
      | trainerUsername   | bob.wilson      |
      | trainerFirstName  | Bob             |
      | trainerLastName   | Wilson          |
      | trainerStatus     | true            |
    And trainer "bob.wilson" has 180 hours in March 2024
    When I remove 30 hours for trainer "bob.wilson" on "2024-03-10"
    Then the trainer should have 150 total hours in March 2024

  Scenario: Remove more hours than available should not result in negative hours
    Given trainer "alice.brown" exists with 50 hours in March 2024
    When I remove 80 hours for trainer "alice.brown" on "2024-03-25"
    Then the trainer should have 0 total hours in March 2024

  Scenario: Retrieve workload summary for existing trainer
    Given trainer "mike.johnson" exists with the following monthly workload:
      | month | year | hours |
      | 3     | 2024 | 120   |
      | 4     | 2024 | 150   |
    When I request workload summary for trainer "mike.johnson" for March 2024
    Then the response status should be 200
    And the summary should contain:
      | trainerUsername  | mike.johnson    |
      | trainerFirstName | Mike            |
      | trainerLastName  | Johnson         |
      | year             | 2024            |
      | month            | 3               |
      | totalHours       | 120             |

  Scenario: Retrieve workload summary for non-existing trainer
    When I request workload summary for trainer "nonexistent.user" for March 2024
    Then the response status should be 404
    And the response should contain error message about trainer not found

  Scenario: Process workload with invalid data
    Given I have an invalid workload request with missing required fields
    When I submit the workload request
    Then the response status should be 400
    And the response should contain validation error messages

  Scenario: Process workload with invalid date
    Given I have a workload request with invalid date format
    When I submit the workload request
    Then the response status should be 400

  Scenario: Process workload with negative duration
    Given I have a workload request with negative training duration
    When I submit the workload request
    Then the response status should be 400

  Scenario: Multiple workload operations for the same trainer and month
    Given trainer "sarah.davis" exists in the system
    When I perform the following workload operations for trainer "sarah.davis" in March 2024:
      | operation | date       | duration |
      | ADD       | 2024-03-01 | 60       |
      | ADD       | 2024-03-15 | 90       |
      | DELETE    | 2024-03-10 | 30       |
      | ADD       | 2024-03-25 | 45       |
    Then the trainer should have 165 total hours in March 2024

  Scenario: Authentication required for API endpoints
    Given I don't have a valid JWT token
    When I try to submit a workload request
    Then the response status should be 401
    And access should be denied

  Scenario: Invalid JWT token
    Given I have an invalid JWT token
    When I try to submit a workload request
    Then the response status should be 401
    And access should be denied

  Scenario: Expired JWT token
    Given I have an expired JWT token
    When I try to submit a workload request
    Then the response status should be 401
    And access should be denied