package edu.kdkce.openelectivefcfs.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.ZonedDateTime;

import java.time.ZoneId;

/**
 * Represents the settings for the Open Elective Allocation Portal.
 * This class contains the opening and closing times for elective allocation.
 */
@DynamoDbBean
public class Settings {
    private String id;

    private ZonedDateTime electiveOpeningTime;

    private ZonedDateTime electiveClosingTime;

    public Settings() {
        // Default values (Example: IST Timezone)
        this.electiveOpeningTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        this.electiveClosingTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).plusDays(1);
    }

    // Getters and Setters
    public ZonedDateTime getElectiveOpeningTime() {
        return electiveOpeningTime;
    }

    public void setElectiveOpeningTime(ZonedDateTime electiveOpeningTime) {
        this.electiveOpeningTime = electiveOpeningTime;
    }

    public ZonedDateTime getElectiveClosingTime() {
        return electiveClosingTime;
    }

    public void setElectiveClosingTime(ZonedDateTime electiveClosingTime) {
        this.electiveClosingTime = electiveClosingTime;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
