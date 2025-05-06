package edu.kdkce.openelectivefcfs.src.util.email;

import edu.kdkce.openelectivefcfs.src.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${frontend.url}")
    private String frontendUrl;
    @Value("${spring.mail.from}")
    private String senderMailId;


    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a verification email to the user with a link to verify their email address.
     *
     * @param user  The user to whom the email should be sent.
     * @param token The verification token associated with the user.
     */
    public void sendVerificationEmail(User user, String token) {
        String verificationUrl = frontendUrl + "/verify?token=" + token;
        String emailSubject = "Verify Your Email to Access Open Elective Allocation Portal";

        String emailMessage = String.format(
                """
                <!DOCTYPE html>
                <html>
                <head>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>
                  body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background: #f8f9fa; }
                  .container { width: 80%%; max-width: 600px; background: #fff; padding: 20px; margin: auto; border-radius: 8px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); }
                  .button-container { text-align: center; margin: 20px 0; }
                  .button { background-color: #4CAF50; color: white; padding: 12px 20px; font-size: 16px; text-decoration: none; display: inline-block; border-radius: 5px; }
                  a { color: #007bff; text-decoration: none; }
                  a:hover { text-decoration: underline; }
                  p { margin: 10px 0; }
                  .footer { font-size: 12px; color: #888; text-align: center; margin-top: 20px; }
                </style>
                </head>
                <body>
                  <div class="container">
                    <h2>Dear %s,</h2>
                    <p>Thank you for registering on the Open Elective Allocation Portal. Please verify your email address to complete your registration.</p>
                    
                    <div class="button-container">
                      <a href="%s" class="button">Verify Email</a>
                    </div>
                    
                    <p>Or, copy and paste this link into your browser:</p>
                    <p><a href="%s">%s</a></p>
                    
                    <p>If you did not register for this portal, you can safely ignore this email.</p>
                    
                    <p><strong>Need help?</strong> Contact our support team at <a href="mailto:support.kdkce@openelective.click">support.kdkce@openelective.click</a>.</p>
                    
                    <p>Regards,<br><strong>Open Elective Administration</strong></p>
                    
                    <p class="footer">This is an auto-generated email, please do not reply.</p>
                  </div>
                </body>
                </html>
                """,
                user.getName(), verificationUrl, verificationUrl, verificationUrl
        );


        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject(emailSubject);
            helper.setText(emailMessage, true); // Enable HTML format
            helper.setFrom(senderMailId);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }


    public void sendPasswordResetMail(User user, String token) {
        String verificationUrl = frontendUrl + "/reset-password?token=" + token;
        String emailSubject = "Reset Your Password for Open Elective Allocation Portal";

        String emailMessage = String.format(
                """
                <!DOCTYPE html>
                <html>
                <head>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>
                  body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background: #f8f9fa; }
                  .container { width: 80%%; max-width: 600px; background: #fff; padding: 20px; margin: auto; border-radius: 8px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); }
                  .button-container { text-align: center; margin: 20px 0; }
                  .button { background-color: #4CAF50; color: white; padding: 12px 20px; font-size: 16px; text-decoration: none; display: inline-block; border-radius: 5px; }
                  a { color: #007bff; text-decoration: none; }
                  a:hover { text-decoration: underline; }
                  p { margin: 10px 0; }
                </style>
                </head>
                <body>
                  <div class="container">
                    <h2>Dear %s,</h2>
                    <p>We received a request to reset your password. Please click the button below to reset it:</p>
                    
                    <div class="button-container">
                      <a href="%s" class="button">Reset Password</a>
                    </div>
                    
                    <p>Or, you can copy and paste this link into your browser:</p>
                    <p><a href="%s">%s</a></p>
                    
                    <p>If you did not request this, please ignore this email.</p>
                    <p><strong>Note:</strong> We don't accept replies to this email. For assistance, contact <a href="mailto:support.kdkce@openelective.click">support.kdkce@openelective.click</a>.</p>
                    
                    <p>Regards,<br><strong>Open Elective Administration</strong></p>
                  </div>
                </body>
                </html>
                """,
                user.getName(), verificationUrl, verificationUrl, verificationUrl
        );


        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject(emailSubject);
            helper.setText(emailMessage, true); // Enable HTML format
            helper.setFrom(senderMailId);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }

    }
}