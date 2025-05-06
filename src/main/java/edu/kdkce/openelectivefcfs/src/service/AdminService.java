package edu.kdkce.openelectivefcfs.src.service;

import edu.kdkce.openelectivefcfs.src.dto.*;
import edu.kdkce.openelectivefcfs.src.model.*;
import edu.kdkce.openelectivefcfs.src.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {
    private final ElectiveRepository electiveRepository;
    private final UserRepository userRepository;
    private final SettingsRepository settingsRepository;
    private final PastAllocationRepository pastAllocationRepository;
    private final AllocationCycleRepository allocationCycleRepository;

    public AdminService(ElectiveRepository electiveRepository, UserRepository userRepository, SettingsRepository settingsRepository, PastAllocationRepository pastAllocationRepository, AllocationCycleRepository allocationCycleRepository) {
        this.electiveRepository = electiveRepository;
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.pastAllocationRepository = pastAllocationRepository;
        this.allocationCycleRepository = allocationCycleRepository;
    }


    public void addElective(CreatElectiveRequest electiveRequest) {
        Elective elective = new Elective();
        elective.setName(electiveRequest.name());
        elective.setDepartmentName(electiveRequest.department());
        elective.setMaxCapacity(electiveRequest.maxCapacity());
        elective.setCapacity(electiveRequest.maxCapacity());
        elective.setDescription(electiveRequest.description());
        electiveRepository.save(elective);
    }

    public void deleteElective(Integer id) {
        electiveRepository.deleteById(id);
    }
    public ElectiveResponse updateElective(Integer id, CreatElectiveRequest electiveRequest) {
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

        // Save updated elective
        electiveRepository.save(elective);
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

    public List<AdminPanelElectiveStatResponse> getDepartmentStats() {
        try{
            List<Elective> electives = electiveRepository.findAll();
            return electives.stream().map(elective -> new AdminPanelElectiveStatResponse(
                    elective.getId(),
                    elective.getDepartmentName(),
                    elective.getName(),
                    elective.getMaxCapacity(),
                    elective.getCapacity(),
                    elective.getMaxCapacity()-elective.getEnrolledStudents().size()
            )).toList();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching elective stats");
        }

    }

    public List<UserProfileResponse> getStudents() {
        List<User> students = userRepository.findAll();
        // Filter out non-students
        return students.stream()
                .filter(user -> user instanceof Student)
                .filter(user -> ((Student) user).getEnabled())
                .map(user -> {
                    Elective elective = ((Student) user).getElective();
                    ElectiveSelected electiveSelected = elective != null ? new ElectiveSelected(
                            elective.getId(),
                            elective.getName(),
                            elective.getDepartmentName()
                    ) : null;
                    return UserProfileResponse.adminPanelStudentData(
                            user.getId(),
                            user.getName(),
                            ((Student) user).getContactNumber(),
                            ((Student) user).getRollNumber(),
                            ((Student) user).getDepartment(),
                            electiveSelected
                    );
                })
                .toList();
    }
    @Transactional
    public void resetAllocations(String cycleName) {
        try {
            // Step 1: Get or create the AllocationCycle
            AllocationCycle allocationCycle = allocationCycleRepository
                    .findByCycleName(cycleName)
                    .orElseGet(() -> {
                        AllocationCycle newCycle = new AllocationCycle();
                        newCycle.setCycleName(cycleName);
                        return allocationCycleRepository.save(newCycle);
                    });

            // Step 2: Prepare PastAllocations
            List<Student> students = userRepository.findAllAllocatedStudents();
            List<PastAllocation> pastAllocations = new ArrayList<>();

            for (Student student : students) {
                PastAllocation pastAllocation = new PastAllocation();
                pastAllocation.setStudentName(student.getName());
                pastAllocation.setRollNumber(student.getRollNumber());
                pastAllocation.setContactNumber(student.getContactNumber());
                pastAllocation.setMailId(student.getEmail());
                pastAllocation.setStudentDepartment(student.getDepartment());

                if (student.getElective() != null) {
                    pastAllocation.setElectiveName(student.getElective().getName());
                    pastAllocation.setElectiveDepartment(student.getElective().getDepartmentName());
                }

                pastAllocation.setAllocationCycle(allocationCycle);
                pastAllocations.add(pastAllocation);
            }

            // Step 3: Save PastAllocations and reset everything
            pastAllocationRepository.saveAll(pastAllocations);
            pastAllocationRepository.flush();

            userRepository.resetStudentElectives();
            userRepository.deleteAllStudents();

            for (Elective elective : electiveRepository.findAll()) {
                elective.setCapacity(elective.getMaxCapacity());
                electiveRepository.save(elective);
            }

        } catch (RuntimeException e) {
            throw new RuntimeException("Error resetting allocations, contact developer if persists");
        }
    }



    public void updateElectiveTimeSettings(ElectiveTimeUpdateRequest electiveTimeUpdateRequest) {
        Settings settings = settingsRepository.findById(1L).orElse(null);

        // Ensure the timestamps are in IST before storing
        ZonedDateTime openingTimeIST = electiveTimeUpdateRequest.allocationStartDate().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime closingTimeIST = electiveTimeUpdateRequest.allocationEndDate().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        if (settings == null) {
            Settings newSettings = new Settings();
            newSettings.setElectiveClosingTime(closingTimeIST);
            newSettings.setElectiveOpeningTime(openingTimeIST);
            settingsRepository.save(newSettings);
            return;
        }

        settings.setElectiveClosingTime(closingTimeIST);
        settings.setElectiveOpeningTime(openingTimeIST);
        settingsRepository.save(settings);
    }

    public ElectiveTimeResponse getElectiveTimeSettings() {
        Settings settings = settingsRepository.findById(1L).orElse(null);

        if (settings == null) {
            throw new RuntimeException("Settings not found");
        }

        // Define time zones
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        ZoneId utcZone = ZoneId.of("UTC"); // Change if your DB stores in a different time zone

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
