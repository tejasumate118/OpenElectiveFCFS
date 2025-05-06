package edu.kdkce.openelectivefcfs.src.service;

import edu.kdkce.openelectivefcfs.src.dto.*;
import edu.kdkce.openelectivefcfs.src.enums.Role;
import edu.kdkce.openelectivefcfs.src.model.*;
import edu.kdkce.openelectivefcfs.src.repository.PasswordResetTokenRepository;
import edu.kdkce.openelectivefcfs.src.repository.UserRepository;
import edu.kdkce.openelectivefcfs.src.repository.VerificationTokenRepository;
import edu.kdkce.openelectivefcfs.src.util.email.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, VerificationTokenRepository tokenRepository, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }
    public LoginResponse login(String email, String password) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if the user is enabled
            if (user instanceof Student student && !student.getEnabled()) {
                return LoginResponse.error("Account is disabled. Please verify your email.");
            }

            // Debug: Print user details
            System.out.println("User found: " + user.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Debug: Print authentication details
            System.out.println("Authentication successful: " + authentication.isAuthenticated());

            String jwtToken = jwtService.generateToken(authentication);

            // Convert to DTO based on Role
            if (user instanceof Student student) {
                Elective elective = student.getElective();
                UserProfileResponse studentResponse = new UserProfileResponse(
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        "STUDENT",
                        student.getDepartment(),
                        student.getRollNumber(),
                        student.getContactNumber(),
                        elective != null ? new ElectiveSelected(elective.getId(), elective.getName(), elective.getDepartmentName()) : null
                );
                return LoginResponse.success(jwtToken, studentResponse);
            }

            if (user.getRoles().contains(Role.ADMIN)) {
                AdminResponse adminResponse = new AdminResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        "ADMIN"
                );
                return LoginResponse.success(jwtToken, adminResponse);
            }

            // If user does not match any role, return an error response
            return LoginResponse.error("Unauthorized user role");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return LoginResponse.error("Invalid email or password");
        }
    }
    @Transactional
    public void createUser(SignupRequest signupRequest) {
        // Check if user is already present with the email
        // If not enabled then delete the user and create new one
        // Check mail ends with @kdkce.edu.in
        if (!signupRequest.email().trim().endsWith("@kdkce.edu.in")) {
            throw new RuntimeException("Invalid email, please use your college email");
        }
        Student user = (Student) userRepository.findByEmail(signupRequest.email().trim()).orElse(null);
        if (user != null && !user.getEnabled()) {
            tokenRepository.deleteAllByUser(user);
            userRepository.delete(user);
        } else if (user != null) {
            throw new RuntimeException("User already exists");
        }
        // Continue with creating new user

        String encodedPassword = passwordEncoder.encode(signupRequest.password());
        Student student = new Student();
        student.setName(signupRequest.name());
        student.setEmail(signupRequest.email());
        student.setRollNumber(signupRequest.rollNumber());
        student.setDepartment(signupRequest.department());
        student.setContactNumber(signupRequest.contactNumber());
        student.setEnabled(false);
        student.setPassword(encodedPassword);
        student.setRoles(Set.of(Role.STUDENT));
        student.setCreatedAt(LocalDateTime.now());
        userRepository.save(student);

        // Generate token & send email
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, student, LocalDateTime.now().plusHours(24));
        //delete previous tokens if any

        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(student, token);
    }

    public String verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));
        // check if token is expired
        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            tokenRepository.delete(verificationToken);
            Student user = (Student)verificationToken.getUser();
            userRepository.delete(user);
            throw new RuntimeException("Token expired");
        }

        Student user = (Student)verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);
        return "Email verified successfully!";
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user, ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).plusMinutes(15));
        //delete previous tokens if any
        passwordResetTokenRepository.deleteAllByUser(user);
        passwordResetTokenRepository.save(passwordResetToken);

        emailService.sendPasswordResetMail(user,token);
        return "Password reset email sent successfully!";
    }

    public String resetPassword(String token, String password) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token"));
        // check if token is expired
        if(passwordResetToken.getExpiryTime().isBefore(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")))){
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new RuntimeException("Token expired");
        }
        User user = passwordResetToken.getUser();
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);
        return "Password reset successfully!";
    }
}
