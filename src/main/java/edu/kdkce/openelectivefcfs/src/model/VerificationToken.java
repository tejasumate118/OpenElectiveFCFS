package edu.kdkce.openelectivefcfs.src.model;


import edu.kdkce.openelectivefcfs.src.converter.LocalDateTimeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;
import java.util.UUID;

@DynamoDbBean
public class VerificationToken {
    private String id;
    private String token;
    private String userId;
    private LocalDateTime expiryDate;


    public VerificationToken() {}

    public VerificationToken(String token, String userId, LocalDateTime expiryDate) {
        this.id = UUID.randomUUID().toString();
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }

    // Getters & Setters

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDbConvertedBy(LocalDateTimeConverter.class)
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}
