package edu.kdkce.openelectivefcfs.src.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import edu.kdkce.openelectivefcfs.src.converter.ZonedDateTimeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
/**
 * Represents a password reset token for a user.
 * This class contains the token, the user associated with it, and the expiry time of the token.
 */

public class PasswordResetToken {
    String id;
    String token;
    String userId;
    ZonedDateTime expiryTime;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, String userId, ZonedDateTime zonedDateTime) {
        this.id = UUID.randomUUID().toString();
        this.token = token;
        this.userId = userId;
        this.expiryTime = zonedDateTime;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbAttribute("token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    @DynamoDbAttribute("expiryTime")
    @DynamoDbConvertedBy(ZonedDateTimeConverter.class)
    public ZonedDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(ZonedDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
}
