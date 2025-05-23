package edu.kdkce.openelectivefcfs.controller;

import edu.kdkce.openelectivefcfs.dto.LoginResponse;
import edu.kdkce.openelectivefcfs.dto.SigninRequest;
import edu.kdkce.openelectivefcfs.dto.SignupRequest;
import edu.kdkce.openelectivefcfs.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        try{
            authService.createUser(signupRequest);
            return ResponseEntity.ok(Map.of("success", true, "message", "User registered successfully"));
        }catch (Exception e){
            logger.error("Error during user registration.{}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }

    }
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody SigninRequest signupRequest) {
        try {
            LoginResponse response = authService.login(signupRequest.email(), signupRequest.password());
            if (!response.success()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during user login with email {}. Message {}",signupRequest.email(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }

    }
    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String token) {
        try {
            String message = authService.verifyEmail(token);
            return ResponseEntity.ok(Map.of("success",true,"message", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            String message = authService.forgotPassword(email);
            return ResponseEntity.ok(Map.of("success",true,"message", message));
        } catch (Exception e) {
            logger.error("Error during user password reset with mail {}. Message {}", email, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable String token, @RequestBody String newPassword) {
        try {
            String message = authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("success",true,"message", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }



}
