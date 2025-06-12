Feature: JMS Message Processing
  As a system
  I want to process workload messages from JMS queue
  So that workload data can be updated asynchronously

  Background:
    Given the workload service is running
    And the JMS queue is available

  Scenario: Process ADD workload message from JMS queue
    Given I have a JMS workload message with the following data:
      | trainerUsername   | emma.clark      |
      | trainerFirstName  | Emma            |
      | trainerLastName   | Clark           |
      | isActive          | true            |
      | trainingDate      | 2024-04-10      |
      | trainingDuration  | 90              |
      | actionType        | ADD             |
    When the message is sent to the workload queue
    Then the message should be consumed successfully
    And trainer "emma.clark" should be created or updated in the database
    And trainer "emma.clark" should have 90 hours in April 2024

  Scenario: Process DELETE workload message from JMS queue
    Given trainer "david.white" exists with 150 hours in April 2024
    And I have a JMS workload message with the following data:
      | trainerUsername   | david.white     |
      | trainerFirstName  | David           |
      | trainerLastName   | White           |
      | isActive          | true            |
      | trainingDate      | 2024-04-15      |
      | trainingDuration  | 40              |
      | actionType        | DELETE          |
    When the message is sent to the workload queue
    Then the message should be consumed successfully
    And trainer "david.white" should have 110 hours in April 2024

  Scenario: Process multiple JMS messages for the same trainer
    Given trainer "lisa.garcia" exists in the system
    When I send multiple JMS messages for trainer "lisa.garcia" in April 2024:
      | actionType | date       | duration |
      | ADD        | 2024-04-05 | 60       |
      | ADD        | 2024-04-12 | 75       |
      | DELETE     | 2024-04-08 | 25       |
    Then all messages should be processed successfully
    And trainer "lisa.garcia" should have 110 hours in April 2024

  Scenario: Process JMS message with transaction ID
    Given I have a JMS workload message with transaction ID "TXN-123456"
    When the message is sent to the workload queue
    Then the message should be consumed successfully
    And the transaction ID should be logged in the application logs

  Scenario: Process JMS message for new trainer
    Given trainer "new.trainer" does not exist in the system
    And I have a JMS workload message for trainer "new.trainer"
      | trainerUsername   | new.trainer     |
      | trainerFirstName  | New             |
      | trainerLastName   | Trainer         |
      | isActive          | true            |
      | trainingDate      | 2024-04-20      |
      | trainingDuration  | 45              |
      | actionType        | ADD             |
    When the message is sent to the workload queue
    Then the message should be consumed successfully
    And trainer "new.trainer" should be created in the database
    And trainer "new.trainer" should have 45 hours in April 2024

  Scenario: Handle JMS message processing error
    Given I have a malformed JMS workload message
    When the message is sent to the workload queue
    Then the message processing should fail
    And an error should be logged
    And the message should be handled according to the error handling strategy

  Scenario: Process JMS message with invalid action type
    Given I have a JMS workload message with invalid action type "INVALID"
    When the message is sent to the workload queue
    Then the message processing should fail
    And an appropriate error should be logged

  Scenario: Verify message consumption order
    Given I have multiple JMS workload messages for trainer "order.test"
    When I send the messages in sequence to the workload queue
    Then the messages should be processed in the correct order
    And the final workload should reflect all operations

  Scenario: JMS message processing with concurrent operations
    Given trainer "concurrent.test" exists in the system
    When I send multiple concurrent JMS messages for trainer "concurrent.test"
    Then all messages should be processed successfully
    And the final workload should be consistent
    And no data should be lost due to concurrent processing

  Scenario: JMS queue unavailable
    Given the JMS queue is not available
    When I try to send a workload message
    Then the operation should handle the failure gracefully
    And appropriate error handling should be triggered