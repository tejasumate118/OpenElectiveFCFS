package edu.kdkce.openelectivefcfs.service;

import edu.kdkce.openelectivefcfs.dto.*;
import edu.kdkce.openelectivefcfs.model.*;
import edu.kdkce.openelectivefcfs.repository.*;
import edu.kdkce.openelectivefcfs.enums.DepartmentName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class AdminService {
    private final ElectiveRepository electiveRepository;
    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final StudentRepository studentRepository;
    private final SettingsRepository settingsRepository;
    private final PastAllocationRepository pastAllocationRepository;
    private final AllocationCycleRepository allocationCycleRepository;

    public AdminService(ElectiveRepository electiveRepository, StudentRepository studentRepository, SettingsRepository settingsRepository, PastAllocationRepository pastAllocationRepository, AllocationCycleRepository allocationCycleRepository) {
        this.electiveRepository = electiveRepository;
        this.studentRepository = studentRepository;
        this.settingsRepository = settingsRepository;
        this.pastAllocationRepository = pastAllocationRepository;
        this.allocationCycleRepository = allocationCycleRepository;
    }


    public void addElective(CreatElectiveRequest electiveRequest) {
        Elective elective = new Elective();
        elective.setId(UUID.randomUUID().toString());
        elective.setName(electiveRequest.name());
        elective.setDepartmentName(electiveRequest.department());
        elective.setMaxCapacity(electiveRequest.maxCapacity());
        elective.setCapacity(electiveRequest.maxCapacity());
        elective.setDescription(electiveRequest.description());
        elective.setEnrolledStudentIds(null);
        elective.setAllowedDepartments(electiveRequest.allowedDepartments());
        electiveRepository.save(elective);
    }

    public void deleteElective(String id) {
        electiveRepository.deleteById(id);
    }

    public ElectiveResponse updateElective(String id, CreatElectiveRequest electiveRequest) {
        // Fetch the elective by name
        Elective elective = electiveRepository.findById(id).orElse(null);
        if (elective == null) {
            throw new RuntimeException("Elective not found");
        }

        int oldMaxCapacity = elective.getMaxCapacity();
        int newMaxCapacity = electiveRequest.maxCapacity();

        // Update the elective details
        elective.setName(electiveRequest.name());
        elective.setDepartmentName(electiveRequest.department());
        elective.setMaxCapacity(newMaxCapacity);

        // Adjust current capacity based on the maxCapacity change
        int capacityDifference = newMaxCapacity - oldMaxCapacity;
        elective.setCapacity(Math.max(0, elective.getCapacity() + capacityDifference));

        elective.setDescription(electiveRequest.description());
        elective.setAllowedDepartments(electiveRequest.allowedDepartments());

        // Save updated elective
        electiveRepository.update(elective);
        //convert to response
        return new ElectiveResponse(
                elective.getId(),
                elective.getName(),
                elective.getDepartmentName(),
                elective.getMaxCapacity(),
                elective.getCapacity(),
                elective.getDescription()
        );
    }

    public List<ElectiveResponse> getElectives() {
        List<Elective> electives = electiveRepository.findAll();
        return electives.stream().map(elective -> new ElectiveResponse(
                elective.getId(),
                elective.getName(),
                elective.getDepartmentName(),
                elective.getMaxCapacity(),
                elective.getCapacity(),
                elective.getDescription()
        )).toList();

    }

    public Set<DepartmentName> allowedDepartments(String id) {
        Elective elective = electiveRepository.findById(id).orElse(null);
        if (elective == null) {
            throw new RuntimeException("Elective not found");
        }
        return elective.getAllowedDepartments();
    }

    public List<AdminPanelElectiveStatResponse> getDepartmentStats() {
        try{
            List<Elective> electives = electiveRepository.findAll();
            return electives.stream().map(elective -> new AdminPanelElectiveStatResponse(
                    elective.getId(),
                    elective.getDepartmentName(),
                    elective.getName(),
                    elective.getMaxCapacity(),
                    elective.getCapacity(),
                    elective.getMaxCapacity() - (elective.getEnrolledStudentIds() != null ? elective.getEnrolledStudentIds().size() : 0)
            )).toList();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching elective stats");
        }

    }

    public List<UserProfileResponse> getStudents() {

        List<Student> students = studentRepository.findAll();
        // Filter out non-students
        return students.stream()
                .filter(Student::getIsEnabled)
                .map(student -> {
                    ElectiveSelected electiveSelected = null;
                    if (student.getElectiveId() != null) {
                        Elective elective = electiveRepository.getElectivesById(student.getElectiveId());
                        if (elective != null) {
                            electiveSelected = new ElectiveSelected(
                                    elective.getId(),
                                    elective.getName(),
                                    elective.getDepartmentName()
                            );
                        }
                    }

                    return UserProfileResponse.adminPanelStudentData(
                            student.getId(),
                            student.getName(),
                            student.getContactNumber(),
                            student.getRollNumber(),
                            student.getDepartment(),
                            electiveSelected,
                            student.getClassRollNumber()
                    );
                })
                .toList();
    }

    public void resetAllocations(String cycleName) {
        try {

            // Step 1: Ensure unique cycle name
            String uniqueCycleName = cycleName;
            int suffix = 1;

            while (allocationCycleRepository.findByCycleName(uniqueCycleName).isPresent()) {
                uniqueCycleName = cycleName + "-" + suffix++;
            }

            AllocationCycle allocationCycle = new AllocationCycle();
            allocationCycle.setId(UUID.randomUUID().toString());
            allocationCycle.setCycleName(uniqueCycleName);
            allocationCycle = allocationCycleRepository.save(allocationCycle);

            // Step 2: Prepare PastAllocations
            List<Student> students = studentRepository.findAllByElectiveIdIsNotNull();
            List<PastAllocation> pastAllocations = new ArrayList<>();

            for (Student student : students) {
                PastAllocation pastAllocation = new PastAllocation();
                pastAllocation.setStudentName(student.getName());
                pastAllocation.setRollNumber(student.getRollNumber());
                pastAllocation.setContactNumber(student.getContactNumber());
                pastAllocation.setMailId(student.getEmail());
                pastAllocation.setStudentDepartment(student.getDepartment());

                if (student.getElectiveId() != null) {
                    Optional<Elective> elective = electiveRepository.findById(student.getElectiveId());
                    if (elective.isEmpty()) {
                        log.warn("Skipping PastAllocation: Elective not found for student {}", student.getName());
                        continue;
                    }
                    pastAllocation.setElectiveName(elective.get().getName());
                    pastAllocation.setElectiveDepartment(elective.get().getDepartmentName());
                }

                pastAllocation.setAllocationCycleId(allocationCycle.getId());
                pastAllocations.add(pastAllocation);
            }

            // Step 3: Save PastAllocations and reset everything
            pastAllocationRepository.saveAll(pastAllocations);
            studentRepository.deleteAll();

            for (Elective elective : electiveRepository.findAll()) {
                elective.setCapacity(elective.getMaxCapacity());
                elective.setEnrolledStudentIds(null);
                electiveRepository.update(elective);
            }

        } catch (RuntimeException e) {
            throw new RuntimeException("Error resetting allocations, contact developer if persists");
        }
    }



    public void updateElectiveTimeSettings(ElectiveTimeUpdateRequest electiveTimeUpdateRequest) {
        Settings settings = settingsRepository.findById("1").orElse(null);

        // Ensure the timestamps are in IST before storing
        ZonedDateTime openingTimeIST = electiveTimeUpdateRequest.allocationStartDate().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime closingTimeIST = electiveTimeUpdateRequest.allocationEndDate().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        if (settings == null) {
            Settings newSettings = new Settings();
            newSettings.setId("1");
            newSettings.setElectiveClosingTime(closingTimeIST);
            newSettings.setElectiveOpeningTime(openingTimeIST);
            settingsRepository.save(newSettings);
            return;
        }

        settings.setElectiveClosingTime(closingTimeIST);
        settings.setElectiveOpeningTime(openingTimeIST);
        settingsRepository.update(settings);
    }

    public ElectiveTimeResponse getElectiveTimeSettings() {
        Settings settings = settingsRepository.findById("1").orElse(null);

        if (settings == null) {
            throw new RuntimeException("Settings not found");
        }

        // Define time zones
        ZoneId istZone = ZoneId.of("Asia/Kolkata");

        // Convert LocalDateTime to ZonedDateTime by assuming it is in UTC
        ZonedDateTime openingTimeIST = settings.getElectiveOpeningTime().withZoneSameInstant(istZone);
        ZonedDateTime closingTimeIST = settings.getElectiveClosingTime().withZoneSameInstant(istZone);

        ZonedDateTime currentTimeIST = ZonedDateTime.now(istZone);

        return new ElectiveTimeResponse(
                openingTimeIST,
                closingTimeIST,
                openingTimeIST.isBefore(currentTimeIST) && currentTimeIST.isBefore(closingTimeIST) // Compare with current IST time
        );
    }
}
