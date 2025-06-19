package com.example.Workload.Service.component.support;

import com.example.Workload.Service.utils.JwtTokenUtil;

import java.time.Month;
import java.util.UUID;

public class TestDataHelper {

    /**
     * Generates a unique transaction ID for testing
     *
     * @return A unique transaction ID string
     */
    public String generateTransactionId() {
        return "test-txn-" + UUID.randomUUID().toString();
    }

    /**
     * Generates a valid JWT token for testing
     *
     * @return A valid JWT token string
     */
    public String generateValidJwtToken() {
        return new JwtTokenUtil().generateToken("John.Trainer");
    }

    /**
     * Generates an expired JWT token for testing
     *
     * @return An expired JWT token string
     */
    public String generateExpiredJwtToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LnVzZXIiLCJuYW1lIjoiVGVzdCBVc2VyIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9.signature";
    }

    /**
     * Converts a month name to its numerical representation
     *
     * @param monthName The name of the month (e.g., "January")
     * @return The month number (1-12)
     */
    public int getMonthNumber(String monthName) {
        try {
            return Month.valueOf(monthName.toUpperCase()).getValue();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid month name: " + monthName);
        }
    }

    /**
     * Check if a JWT token is valid.
     *
     * @param token The JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean isValidJwtToken(String token) {
        return token != null && token.equals(generateValidJwtToken());
    }
}
