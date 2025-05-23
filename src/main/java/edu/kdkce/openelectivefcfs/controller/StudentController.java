package edu.kdkce.openelectivefcfs.controller;

import edu.kdkce.openelectivefcfs.dto.ElectiveResponse;
import edu.kdkce.openelectivefcfs.dto.UserUpdateRequest;
import edu.kdkce.openelectivefcfs.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentService studentService;
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/getElectives")
    public ResponseEntity<?> getElectives() {
        try{
            List<ElectiveResponse> allowedElectives = studentService.getElectives();
            return ResponseEntity.ok(allowedElectives);
        }catch (Exception e){
            logger.error("Error fetching electives.{}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }

    @PostMapping("/elective/select")
    public ResponseEntity<?> selectElective(@RequestBody String electiveId) {
        try{
            studentService.selectElective(electiveId);
            return ResponseEntity.ok("Elective selected successfully");
        }catch (Exception e){
            logger.error("Error fetching elective for elective id {}. {}",electiveId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try{
            return ResponseEntity.ok(studentService.getProfile());
        }catch (Exception e){
            logger.error("Error fetching profile.{}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateRequest request) {
        try{
            studentService.updateProfile(request);
            return ResponseEntity.ok("Profile updated successfully");
        }catch (Exception e){
            logger.error("Error updating profile.{}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }
    @PutMapping("/change-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String,String> request) {
        try{
            studentService.changePassword(request);
            return ResponseEntity.ok("Password changed successfully");
        }catch (Exception e){
            logger.error("Error changing password.{}",e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }
    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        try{
            return ResponseEntity.ok(studentService.getElectiveTimeSettings());
        }catch (Exception e){
            logger.error("Error fetching settings.{}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }
}
