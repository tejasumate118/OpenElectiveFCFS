package edu.kdkce.openelectivefcfs.src.service;

import edu.kdkce.openelectivefcfs.src.dto.*;
import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;
import edu.kdkce.openelectivefcfs.src.model.Elective;
import edu.kdkce.openelectivefcfs.src.model.Settings;
import edu.kdkce.openelectivefcfs.src.model.Student;
import edu.kdkce.openelectivefcfs.src.repository.ElectiveRepository;
import edu.kdkce.openelectivefcfs.src.repository.SettingsRepository;
import edu.kdkce.openelectivefcfs.src.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private final UserRepository userRepository;
    private final ElectiveRepository electiveRepository;
    private final PasswordEncoder passwordEncoder;
    private final SettingsRepository settingsRepository;

    @Autowired
    public StudentService(UserRepository userRepository, ElectiveRepository electiveRepository, PasswordEncoder passwordEncoder, SettingsRepository settingsRepository) {
        this.userRepository = userRepository;
        this.electiveRepository = electiveRepository;
        this.passwordEncoder = passwordEncoder;
        this.settingsRepository = settingsRepository;
    }
    public List<ElectiveResponse> getElectives() {
        List<Elective> electives = electiveRepository.findAll();
        //check user department first
        String mail = SecurityContextHolder.getContext().getAuthentication().getName();
        DepartmentName department = userRepository.findByEmail(mail)
                .filter(user -> user instanceof Student)
                .map(user -> ((Student) user).getDepartment())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //filter electives based on department
        Set<DepartmentName> allowedDepartments = allowedElectiveDepartment(department);
        List<Elective> allowedElectives = electives.stream()
                .filter(elective -> allowedDepartments.contains(elective.getDepartmentName()))
                .toList();
        //Convert to ElectiveResponse
        return allowedElectives.stream()
                .map(elective -> new ElectiveResponse(
                        elective.getId(),
                        elective.getName(),
                        elective.getDepartmentName(),
                        elective.getMaxCapacity(),
                        elective.getCapacity(),
                        elective.getDescription()
                ))
                .toList();

    }

    private Set<DepartmentName> allowedElectiveDepartment(DepartmentName department) {
        return Arrays.stream(DepartmentName.values())
                .filter(d -> !d.equals(department)) // Exclude the student's own department
                .filter(d -> !(isCSEorIT(department) && isCSEorIT(d))) // CSE & IT cannot take CSE/IT electives
                .collect(Collectors.toSet()); // Convert to Set
    }

    private boolean isCSEorIT(DepartmentName department) {
        return department == DepartmentName.CSE || department == DepartmentName.IT;
    }

    @Transactional
    public void selectElective(Integer electiveId) {
        String mail = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = userRepository.findByEmail(mail)
                .filter(user -> user instanceof Student)
                .map(user -> (Student) user)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //check allocation is open
        Settings settings = settingsRepository.findById(1L).orElse(new Settings());
        if (ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).isBefore(settings.getElectiveOpeningTime()) || ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(settings.getElectiveClosingTime())) {
            throw new RuntimeException("Elective allocation is not open");
        }

        // Fetch elective with pessimistic lock
        Elective elective = electiveRepository.findByIdWithLock(electiveId)
                .orElseThrow(() -> new RuntimeException("Elective not found"));

        // Double-check if student is allowed to take the elective
        if (!allowedElectiveDepartment(student.getDepartment()).contains(elective.getDepartmentName())) {
            throw new RuntimeException("Student cannot enroll in elective");
        }

        // Check if student is already enrolled in this elective
        if (student.getElective() != null && student.getElective().equals(elective)) {
            throw new RuntimeException("Student is already enrolled in this elective");
        }

        // Check if elective has available capacity
        if (elective.getCapacity() <= 0) {
            throw new RuntimeException("Elective is full");
        }

        // Release seat from previous elective
        Elective prevElective = student.getElective();
        if (prevElective != null) {
            prevElective.setCapacity(prevElective.getCapacity() + 1);
            electiveRepository.save(prevElective);
        }

        // Final department-based restriction check
        if (!student.canEnrollIn(elective)) {
            throw new RuntimeException("Cannot enroll in this elective due to department restrictions");
        }

        // Assign new elective and update capacity
        student.setElective(elective);
        elective.setCapacity(elective.getCapacity() - 1);

        userRepository.save(student);
        electiveRepository.save(elective);
    }



    public UserProfileResponse getProfile() {
        String mail = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = (Student) userRepository.findByEmail(mail).orElse(null);
        if (student == null) {
            throw new RuntimeException("User not found");
        }
        Elective elective = student.getElective();
        return new UserProfileResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                "STUDENT",
                student.getDepartment(),
                student.getRollNumber(),
                student.getContactNumber(),
                elective != null ? new ElectiveSelected(elective.getId(), elective.getName(), elective.getDepartmentName()) : null

        );


    }

    public void updateProfile(UserUpdateRequest request) {
        String mail = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = (Student) userRepository.findByEmail(mail).orElse(null);
        if(student == null) {
            throw new RuntimeException("User not found");
        }
        student.setName(request.name());
        student.setContactNumber(request.contactNumber());
        student.setRollNumber(request.rollNumber());
        userRepository.save(student);
    }

    @Transactional
    public void changePassword(Map<String, String> request) {
        String mail = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = (Student) userRepository.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (!passwordEncoder.matches(currentPassword, student.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        student.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(student);
    }


    public ElectiveTimeResponse getElectiveTimeSettings() {
        Settings settings = settingsRepository.findById(1L).orElse(new Settings());
        return new ElectiveTimeResponse(
                settings.getElectiveOpeningTime(),
                settings.getElectiveClosingTime(),
                settings.getElectiveOpeningTime().isBefore(LocalDateTime.now().atZone(ZoneId.systemDefault()))
        );

    }
}
