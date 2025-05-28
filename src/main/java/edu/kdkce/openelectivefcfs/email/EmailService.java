package edu.kdkce.openelectivefcfs.email;

import edu.kdkce.openelectivefcfs.model.Student;
import edu.kdkce.openelectivefcfs.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${aws.sqs.emailQueueUrl}")
    private String queueUrl;  // SQS Queue URL

    private final SqsClient sqsClient;
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    /**
     * Sends a verification email to the user by publishing a message to the SES queue.
     *
     * @param user  The user to whom the email should be sent.
     * @param token The verification token associated with the user.
     */
    public void sendVerificationEmail(User user, String token) {
        String verificationUrl = frontendUrl + "/verify?token=" + token;
        String emailSubject = "Verify Your Email to Access Open Elective Allocation Portal";

        String emailMessage = String.format(
                """
                <p>Dear %s,</p>
                <p>Thank you for registering on the Open Elective Allocation Portal. Please verify your email address by clicking the link below:</p>
                <a href="%s">%s</a>
                <p>If you did not register, you can safely ignore this email.</p>
                """,
                user.getName(), verificationUrl, verificationUrl
        );

        publishToSqs(user.getEmail(), emailSubject, emailMessage);
    }
    public void sendVerificationEmail(Student user, String token) {
        String verificationUrl = frontendUrl + "/verify?token=" + token;
        String emailSubject = "Verify Your Email to Access Open Elective Allocation Portal";

        String emailMessage = String.format(
                """
                <p>Dear %s,</p>
                <p>Thank you for registering on the Open Elective Allocation Portal. Please verify your email address by clicking the link below:</p>
                <a href="%s">%s</a>
                <p>If you did not register, you can safely ignore this email.</p>
                """,
                user.getName(), verificationUrl, verificationUrl
        );

        publishToSqs(user.getEmail(), emailSubject, emailMessage);
    }

    /**
     * Sends a password reset email to the user by publishing a message to the SES queue.
     *
     * @param user  The user to whom the email should be sent.
     * @param token The password reset token associated with the user.
     */
    public void sendPasswordResetMail(User user, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String emailSubject = "Reset Your Password for Open Elective Allocation Portal";

        String emailMessage = String.format(
                """
                <p>Dear %s,</p>
                <p>We received a request to reset your password. Please click the link below to reset it:</p>
                <a href="%s">%s</a>
                <p>If you did not request this, please ignore this email.</p>
                """,
                user.getName(), resetUrl, resetUrl
        );

        publishToSqs(user.getEmail(), emailSubject, emailMessage);
    }
    public void sendPasswordResetMail(Student user, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String emailSubject = "Reset Your Password for Open Elective Allocation Portal";

        String emailMessage = String.format(
                """
                <p>Dear %s,</p>
                <p>We received a request to reset your password. Please click the link below to reset it:</p>
                <a href="%s">%s</a>
                <p>If you did not request this, please ignore this email.</p>
                """,
                user.getName(), resetUrl, resetUrl
        );

        publishToSqs(user.getEmail(), emailSubject, emailMessage);
    }


    /**
     * Publishes the email details to the SES queue.
     *
     * @param toEmail   The recipient's email address.
     * @param subject   The email subject.
     * @param message   The email content.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    private void publishToSqs(String toEmail, String subject, String message) {
        try {
            Map<String, String> emailPayload = new HashMap<>();
            emailPayload.put("to", toEmail);
            emailPayload.put("subject", subject);
            emailPayload.put("text", message);

            String messageBody = objectMapper.writeValueAsString(emailPayload);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(sendMessageRequest);
        } catch (Exception e) {
            // Handle or log the exception as needed
            logger.info("Error pushing mail into queue:  {}",e.getMessage());

        }
    }
}