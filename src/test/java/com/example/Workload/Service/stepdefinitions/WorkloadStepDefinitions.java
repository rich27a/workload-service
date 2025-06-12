package com.example.Workload.Service.stepdefinitions;

import com.example.Workload.Service.component.config.TestConfiguration;
import com.example.Workload.Service.component.support.TestDataHelper;
import com.example.Workload.Service.models.ActionType;
import com.example.Workload.Service.models.WorkloadRequest;
import com.example.Workload.Service.models.documents.TrainerSummary;
import com.example.Workload.Service.repositories.mongo.TrainerSummaryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = TestConfiguration.class)
public class WorkloadStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private TrainerSummaryRepository trainerSummaryRepository;

    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private WorkloadRequest currentRequest;
    private Response lastResponse;
    private String currentJwtToken;
    private String currentTransactionId;

    @Given("the workload service is running")
    public void theWorkloadServiceIsRunning() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        Response healthCheck = given()
                .when()
                .get("/actuator/health")
                .then()
                .extract().response();

        assertThat(healthCheck.getStatusCode()).isIn(200, 404); // 404 is OK if actuator not configured
    }

    @Given("I have a valid JWT token")
    public void iHaveAValidJwtToken() {
        currentJwtToken = testDataHelper.generateValidJwtToken();
        assertThat(currentJwtToken).isNotNull();
    }

    @Given("I don't have a valid JWT token")
    public void iDontHaveAValidJwtToken() {
        currentJwtToken = null;
    }

    @Given("I have an invalid JWT token")
    public void iHaveAnInvalidJwtToken() {
        currentJwtToken = "invalid.jwt.token";
    }

    @Given("I have an expired JWT token")
    public void iHaveAnExpiredJwtToken() {
        currentJwtToken = testDataHelper.generateExpiredJwtToken();
    }

    @Given("I have a workload request for trainer {string}")
    public void iHaveAWorkloadRequestForTrainer(String username, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();

        currentRequest = new WorkloadRequest(
                data.get("trainerUsername"),
                data.get("trainerFirstName"),
                data.get("trainerLastName"),
                Boolean.parseBoolean(data.get("isActive")),
                LocalDate.parse(data.get("trainingDate")),
                Integer.parseInt(data.get("trainingDuration")),
                ActionType.valueOf(data.get("actionType"))
        );

        currentTransactionId = testDataHelper.generateTransactionId();
    }

    @Given("trainer {string} already exists in the system")
    public void trainerAlreadyExistsInTheSystem(String username, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();

        TrainerSummary trainerSummary = new TrainerSummary();
        trainerSummary.setTrainerUsername(username);
        trainerSummary.setTrainerFirstName(data.get("trainerFirstName"));
        trainerSummary.setTrainerLastName(data.get("trainerLastName"));
        trainerSummary.setTrainerStatus(Boolean.parseBoolean(data.get("trainerStatus")));

        trainerSummaryRepository.save(trainerSummary);
    }

    @Given("trainer {string} has {int} hours in {word} {int}")
    public void trainerHasHoursInMonth(String username, int hours, String monthName, int year) {
        int month = testDataHelper.getMonthNumber(monthName);

        TrainerSummary trainerSummary = trainerSummaryRepository.findByTrainerUsername(username)
                .orElseGet(() -> {
                    TrainerSummary newTrainer = new TrainerSummary();
                    newTrainer.setTrainerUsername(username);
                    newTrainer.setTrainerFirstName("Test");
                    newTrainer.setTrainerLastName("Trainer");
                    newTrainer.setTrainerStatus(true);
                    return newTrainer;
                });

        TrainerSummary.YearSummary yearSummary = trainerSummary.getOrCreateYearSummary(year);
        TrainerSummary.MonthSummary monthSummary = yearSummary.getOrCreateMonthSummary(month);
        monthSummary.setTrainingsSummaryDuration(hours);

        trainerSummaryRepository.save(trainerSummary);
    }

    @Given("trainer {string} exists with the following workload")
    public void trainerExistsWithTheFollowingWorkload(String username, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();

        TrainerSummary trainerSummary = new TrainerSummary();
        trainerSummary.setTrainerUsername(username);
        trainerSummary.setTrainerFirstName(data.get("trainerFirstName"));
        trainerSummary.setTrainerLastName(data.get("trainerLastName"));
        trainerSummary.setTrainerStatus(Boolean.parseBoolean(data.get("trainerStatus")));

        trainerSummaryRepository.save(trainerSummary);
    }

    @Given("trainer {string} exists with {int} hours in {word} {int}")
    public void trainerExistsWithHoursInMonth(String username, int hours, String monthName, int year) {
        trainerHasHoursInMonth(username, hours, monthName, year);
    }

    @Given("trainer {string} exists with the following monthly workload:")
    public void trainerExistsWithTheFollowingMonthlyWorkload(String username, DataTable dataTable) {
        TrainerSummary trainerSummary = new TrainerSummary();
        trainerSummary.setTrainerUsername(username);
        trainerSummary.setTrainerFirstName("Mike");
        trainerSummary.setTrainerLastName("Johnson");
        trainerSummary.setTrainerStatus(true);

        List<Map<String, String>> workloadData = dataTable.asMaps();
        for (Map<String, String> row : workloadData) {
            int month = Integer.parseInt(row.get("month"));
            int year = Integer.parseInt(row.get("year"));
            int hours = Integer.parseInt(row.get("hours"));

            TrainerSummary.YearSummary yearSummary = trainerSummary.getOrCreateYearSummary(year);
            TrainerSummary.MonthSummary monthSummary = yearSummary.getOrCreateMonthSummary(month);
            monthSummary.setTrainingsSummaryDuration(hours);
        }

        trainerSummaryRepository.save(trainerSummary);
    }

    @Given("I have an invalid workload request with missing required fields")
    public void iHaveAnInvalidWorkloadRequestWithMissingRequiredFields() {
        currentRequest = new WorkloadRequest(
                "",
                null,
                "Doe",
                true,
                LocalDate.now(),
                120,
                ActionType.ADD
        );
    }

    @Given("I have a workload request with invalid date format")
    public void iHaveAWorkloadRequestWithInvalidDateFormat() {
        currentRequest = null;
    }

    @Given("I have a workload request with negative training duration")
    public void iHaveAWorkloadRequestWithNegativeTrainingDuration() {
        currentRequest = new WorkloadRequest(
                "test.trainer",
                "Test",
                "Trainer",
                true,
                LocalDate.now(),
                -30, // negative duration
                ActionType.ADD
        );
    }

    @Given("trainer {string} exists in the system")
    public void trainerExistsInTheSystem(String username) {
        TrainerSummary trainerSummary = new TrainerSummary();
        trainerSummary.setTrainerUsername(username);
        trainerSummary.setTrainerFirstName("Sarah");
        trainerSummary.setTrainerLastName("Davis");
        trainerSummary.setTrainerStatus(true);

        trainerSummaryRepository.save(trainerSummary);
    }

    @When("I submit the workload request")
    public void iSubmitTheWorkloadRequest() {
        var requestSpec = given()
                .contentType(ContentType.JSON)
                .header("X-Transaction-ID", currentTransactionId);

        if (currentJwtToken != null) {
            requestSpec.header("Authorization", "Bearer " + currentJwtToken);
        }

        if (currentRequest != null) {
            requestSpec.body(currentRequest);
        } else {
            requestSpec.body("{\"trainerUsername\":\"test\",\"trainingDate\":\"invalid-date\"}");
        }

        lastResponse = requestSpec
                .when()
                .post("/api/workload")
                .then()
                .extract().response();
    }

    @When("I add {int} hours for trainer {string} on {string}")
    public void iAddHoursForTrainerOn(int hours, String username, String date) {
        WorkloadRequest request = new WorkloadRequest(
                username,
                "Test",
                "Trainer",
                true,
                LocalDate.parse(date),
                hours,
                ActionType.ADD
        );

        lastResponse = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + currentJwtToken)
                .header("X-Transaction-ID", testDataHelper.generateTransactionId())
                .body(request)
                .when()
                .post("/api/workload")
                .then()
                .extract().response();
    }

    @When("I remove {int} hours for trainer {string} on {string}")
    public void iRemoveHoursForTrainerOn(int hours, String username, String date) {
        WorkloadRequest request = new WorkloadRequest(
                username,
                "Test",
                "Trainer",
                true,
                LocalDate.parse(date),
                hours,
                ActionType.DELETE
        );

        lastResponse = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + currentJwtToken)
                .header("X-Transaction-ID", testDataHelper.generateTransactionId())
                .body(request)
                .when()
                .post("/api/workload")
                .then()
                .extract().response();
    }

    @When("I request workload summary for trainer {string} for {word} {int}")
    public void iRequestWorkloadSummaryForTrainerFor(String username, String monthName, int year) {
        int month = testDataHelper.getMonthNumber(monthName);

        var requestSpec = given()
                .header("X-Transaction-ID", testDataHelper.generateTransactionId());

        if (currentJwtToken != null) {
            requestSpec.header("Authorization", "Bearer " + currentJwtToken);
        }

        lastResponse = requestSpec
                .when()
                .get("/api/workload/{username}/{year}/{month}", username, year, month)
                .then()
                .extract().response();
    }

    @When("I perform the following workload operations for trainer {string} in {word} {int}:")
    public void iPerformTheFollowingWorkloadOperationsForTrainerIn(String username, String monthName, int year, DataTable dataTable) {
        List<Map<String, String>> operations = dataTable.asMaps();

        for (Map<String, String> operation : operations) {
            ActionType actionType = ActionType.valueOf(operation.get("operation"));
            LocalDate date = LocalDate.parse(operation.get("date"));
            int duration = Integer.parseInt(operation.get("duration"));

            WorkloadRequest request = new WorkloadRequest(
                    username,
                    "Sarah",
                    "Davis",
                    true,
                    date,
                    duration,
                    actionType
            );

            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + currentJwtToken)
                    .header("X-Transaction-ID", testDataHelper.generateTransactionId())
                    .body(request)
                    .when()
                    .post("/api/workload")
                    .then()
                    .statusCode(200);
        }
    }

    @When("I try to submit a workload request")
    public void iTryToSubmitAWorkloadRequest() {
        WorkloadRequest request = new WorkloadRequest(
                "test.trainer",
                "Test",
                "Trainer",
                true,
                LocalDate.now(),
                60,
                ActionType.ADD
        );

        var requestSpec = given()
                .contentType(ContentType.JSON)
                .header("X-Transaction-ID", testDataHelper.generateTransactionId())
                .body(request);

        if (currentJwtToken != null) {
            requestSpec.header("Authorization", "Bearer " + currentJwtToken);
        }

        lastResponse = requestSpec
                .when()
                .post("/api/workload")
                .then()
                .extract().response();
    }
}
