package edu.kdkce.openelectivefcfs.src.service;

import edu.kdkce.openelectivefcfs.src.dto.*;
import edu.kdkce.openelectivefcfs.src.enums.Role;
import edu.kdkce.openelectivefcfs.src.model.*;
import edu.kdkce.openelectivefcfs.src.repository.*;
import edu.kdkce.openelectivefcfs.src.util.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final StudentRepository studentRepository;
    private final ElectiveRepository electiveRepository;

    @Autowired
    AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, VerificationTokenRepository tokenRepository, PasswordResetTokenRepository passwordResetTokenRepository, StudentRepository studentRepository, ElectiveRepository electiveRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.studentRepository = studentRepository;
        this.electiveRepository = electiveRepository;
    }
    public LoginResponse login(String email, String password) {
        try {
            // Convert email to lowercase for case-insensitive comparison
            email = email.trim().toLowerCase();

            // Step 1: Try admin login (User table)
            Optional<User> adminOptional = userRepository.findByEmail(email);
            if (adminOptional.isPresent()) {
                User admin = adminOptional.get();

                // Authenticate admin
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, password)
                );

                String jwtToken = jwtService.generateToken(authentication);

                if (admin.getRoles().contains(Role.ADMIN)) {
                    AdminResponse adminResponse = new AdminResponse(
                            admin.getId(),
                            admin.getName(),
                            admin.getEmail(),
                            "ADMIN"
                    );
                    return LoginResponse.success(jwtToken, adminResponse);
                }

                return LoginResponse.error("Unauthorized, Access Denied");
            }

            // Step 2: Try student login (Student table)
            Optional<Student> studentOptional = studentRepository.findByEmail(email);
            if (studentOptional.isEmpty()) {
                return LoginResponse.error("Invalid email or password");
            }

            Student student = studentOptional.get();

            if (!student.getIsEnabled()) {
                return LoginResponse.error("Account is disabled. Please verify your email.");
            }

            // Authenticate student
            if (!passwordEncoder.matches(password, student.getPassword())) {
                return LoginResponse.error("Invalid email or password");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            String jwtToken = jwtService.generateToken(authentication);

            Elective elective = null;
            if (student.getElectiveId() != null) {
                elective = electiveRepository.findById(student.getElectiveId()).orElse(null);
            }

            UserProfileResponse studentResponse = new UserProfileResponse(
                    student.getId(),
                    student.getName(),
                    student.getEmail(),
                    "STUDENT",
                    student.getDepartment(),
                    student.getRollNumber(),
                    student.getContactNumber(),
                    elective != null ? new ElectiveSelected(elective.getId(), elective.getName(), elective.getDepartmentName()) : null,
                    student.getClassRollNumber()
            );

            return LoginResponse.success(jwtToken, studentResponse);

        } catch (Exception e) {
            logger.error("Login failed for email: {}. Error: {}", email, e.getMessage());
            return LoginResponse.error("Invalid email or password");
        }
    }

    public void createUser(SignupRequest signupRequest) {
       //only college mails
        if (!signupRequest.email().trim().endsWith("@kdkce.edu.in")) {
            throw new RuntimeException("Invalid email, please use your college email");
        }
        //Checking if user exists
        Student existingStudent = studentRepository.findByEmail(signupRequest.email().trim()).orElse(null);
        if (existingStudent != null && !existingStudent.getIsEnabled()) {
            tokenRepository.deleteAllByUserId(existingStudent.getId());
            studentRepository.deleteById(existingStudent.getId());
        } else if (existingStudent != null && existingStudent.getIsEnabled()) {
            throw new RuntimeException("User already exists");
        }
        // Continue with creating new user

        String encodedPassword = passwordEncoder.encode(signupRequest.password());
        Student student = new Student();
        student.setId(UUID.randomUUID().toString());
        student.setName(signupRequest.name());
        student.setEmail(signupRequest.email().trim().toLowerCase());
        student.setRollNumber(signupRequest.rollNumber());
        student.setDepartment(signupRequest.department());
        student.setContactNumber(signupRequest.contactNumber());
        student.setIsEnabled(false);
        student.setPassword(encodedPassword);
        student.setRoles(Set.of(Role.STUDENT));
        student.setCreatedAt(LocalDateTime.now());
        studentRepository.save(student);

        // Generate token & send email
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, student.getId(), LocalDateTime.now(ZoneId.of("UTC")).plusHours(24*7));
        //delete previous tokens if any
        tokenRepository.deleteAllByUserId(student.getId());

        tokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(student, token);
    }


    public String verifyEmail(String token) {
        Optional<VerificationToken> verificationTokenOptional = tokenRepository.findByToken(token);
        System.out.println("Token: " + verificationTokenOptional);
        if(verificationTokenOptional.isEmpty()){
            throw new RuntimeException("Invalid token");
        }
        VerificationToken verificationToken = verificationTokenOptional.get();
        // check if token is expired
        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now(ZoneId.of("UTC")))){
            tokenRepository.delete(verificationToken);
            if(studentRepository.existsById(verificationToken.getUserId()))
                studentRepository.deleteById(verificationToken.getUserId());
            throw new RuntimeException("Token expired");
        }

        Student student = studentRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        student.setIsEnabled(true);
        studentRepository.update(student);
        tokenRepository.delete(verificationToken);
        return "Email verified successfully!";
    }


    public String forgotPassword(String email) {
        Optional < User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user  = userOptional.get();
            String token = UUID.randomUUID().toString();
            PasswordResetToken passwordResetToken = new
                    PasswordResetToken(token, user.getId(), ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).plusMinutes(15));
            //delete previous tokens if any
            passwordResetTokenRepository.deleteAllByUserId(user.getId());
            passwordResetTokenRepository.save(passwordResetToken);
            emailService.sendPasswordResetMail(user,token);
            return "Password reset email sent successfully!";
        }
        Optional<Student> studentOptional = studentRepository.findByEmail(email);
        if (studentOptional.isPresent()) {
            Student student = studentOptional.get();
            String token = UUID.randomUUID().toString();
            PasswordResetToken passwordResetToken = new
                    PasswordResetToken(token, student.getId(), ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).plusMinutes(15));
            //delete previous tokens if any
            passwordResetTokenRepository.deleteAllByUserId(student.getId());
            passwordResetTokenRepository.save(passwordResetToken);
            emailService.sendPasswordResetMail(student,token);
            return "Password reset email sent successfully!";
        }
        throw new RuntimeException("User not found");
    }


    public String resetPassword(String token, String password) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token"));
        // check if token is expired
        if(passwordResetToken.getExpiryTime().isBefore(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")))){
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new RuntimeException("Token expired");
        }
        Student student = studentRepository.findById(passwordResetToken.getUserId()).orElse(null);
        String encodedPassword = passwordEncoder.encode(password);
        if(student == null) {
            User user = userRepository.findById(passwordResetToken.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
            user.setPassword(encodedPassword);
            userRepository.update(user);
        }else {
            student.setPassword(encodedPassword);
            student.setIsEnabled(true);
            studentRepository.update(student);
        }
        passwordResetTokenRepository.delete(passwordResetToken);
        return "Password reset successfully!";
    }
}
