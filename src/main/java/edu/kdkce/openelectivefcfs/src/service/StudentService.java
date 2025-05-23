package edu.kdkce.openelectivefcfs.src.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kdkce.openelectivefcfs.src.dto.*;
import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;
import edu.kdkce.openelectivefcfs.src.model.Elective;
import edu.kdkce.openelectivefcfs.src.model.Settings;
import edu.kdkce.openelectivefcfs.src.model.Student;
import edu.kdkce.openelectivefcfs.src.repository.ElectiveRepository;
import edu.kdkce.openelectivefcfs.src.repository.SettingsRepository;
import edu.kdkce.openelectivefcfs.src.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private final ElectiveRepository electiveRepository;
    private final PasswordEncoder passwordEncoder;
    private final SettingsRepository settingsRepository;
    private final DynamoDbClient amazonDynamoDB;
    private final SqsClient sqsClient;

    @Value("${aws.sqs.electiveUpdateQueueUrl}")
    private String electiveUpdateQueueUrl;


    @Autowired
    public StudentService(StudentRepository studentRepository, ElectiveRepository electiveRepository, PasswordEncoder passwordEncoder, SettingsRepository settingsRepository, DynamoDbClient amazonDynamoDB, SqsClient sqsClient) {
        this.studentRepository = studentRepository;
        this.electiveRepository = electiveRepository;
        this.passwordEncoder = passwordEncoder;
        this.settingsRepository = settingsRepository;
        this.amazonDynamoDB = amazonDynamoDB;
        this.sqsClient = sqsClient;
    }
    public List<ElectiveResponse> getElectives() {
        List<Elective> electives = electiveRepository.findAll();
        //checking user department first
        String mail = SecurityContextHolder.getContext().getAuthentication().getName();
        DepartmentName department = studentRepository.findByEmail(mail)
                .map(Student::getDepartment)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Elective> allowedElectives = electives.stream()
                .filter(elective -> elective.getAllowedDepartments().contains(department))
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





    /**
     * Selects an elective for the student.
     *
     * @param electiveId The ID of the elective to select.
     * @throws RuntimeException if the elective selection fails due to various reasons.
     */

    public void selectElective(String electiveId) {
        electiveId = electiveId.replace("\"", "").trim();

        String mail = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(mail).orElseThrow(() -> new RuntimeException("User not found"));

        // Check if allocation is open
        Settings settings = settingsRepository.findById("1").orElse(new Settings());
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        if (now.isBefore(settings.getElectiveOpeningTime()) || now.isAfter(settings.getElectiveClosingTime())) {
            throw new RuntimeException("Elective allocation is not open");
        }

        // Get selected elective

        Elective elective = electiveRepository.findById(electiveId)
                .orElseThrow(() -> new RuntimeException("Elective not found"));

        if (!elective.getAllowedDepartments().contains(student.getDepartment())) {
            throw new RuntimeException("Student cannot enroll in elective");
        }


        if (student.getElectiveId() != null && student.getElectiveId().equals(elective.getId())) {
            throw new RuntimeException("Student is already enrolled in this elective");
        }

        if (elective.getCapacity() <= 0) {
            throw new RuntimeException("Elective is full");
        }

        // Handle old elective
        Elective prevElective = null;
        if (student.getElectiveId() != null) {
            prevElective = electiveRepository.findById(student.getElectiveId()).orElse(null);
            if (prevElective != null) {
                prevElective.setCapacity(prevElective.getCapacity() + 1);
            }
        }

        // Update current elective and student
        elective.setCapacity(elective.getCapacity() - 1);
        elective.setVersion(elective.getVersion()); // Keep current version for optimistic locking
        student.setElectiveId(elective.getId());

        // Prepare transaction items
        List<TransactWriteItem> transactItems = new ArrayList<>();
        String studentId = student.getId();


        // Update new elective
        transactItems.add(TransactWriteItem.builder()
                .update(Update.builder()
                        .tableName("Elective")
                        .key(Map.of("id", AttributeValue.builder().s(elective.getId()).build()))
                        .updateExpression("SET #capacity = :newCapacity, version = :newVersion ADD #students :studentToAdd")
                        .conditionExpression("version = :expectedVersion")
                        .expressionAttributeNames(Map.of(
                                "#capacity", "capacity",
                                "#students", "enrolledStudentIds"
                        ))
                        .expressionAttributeValues(Map.of(
                                ":newCapacity", AttributeValue.builder().n(String.valueOf(elective.getCapacity())).build(),
                                ":expectedVersion", AttributeValue.builder().n(String.valueOf(elective.getVersion())).build(),
                                ":newVersion", AttributeValue.builder().n(String.valueOf(elective.getVersion() + 1)).build(),
                                ":studentToAdd", AttributeValue.builder().ss(studentId).build()
                        ))
                        .build())
                .build());

        // Update old elective (if exists)
        if (prevElective != null) {
            transactItems.add(TransactWriteItem.builder()
                    .update(Update.builder()
                            .tableName("Elective")
                            .key(Map.of("id", AttributeValue.builder().s(prevElective.getId()).build()))
                            .updateExpression("SET #capacity = :cap DELETE #students :studentToRemove")
                            .expressionAttributeNames(Map.of(
                                    "#capacity", "capacity",
                                    "#students", "enrolledStudentIds"
                            ))
                            .expressionAttributeValues(Map.of(
                                    ":cap", AttributeValue.builder().n(String.valueOf(prevElective.getCapacity())).build(),
                                    ":studentToRemove", AttributeValue.builder().ss(studentId).build()
                            ))
                            .build())
                    .build());
        }

        // Put updated student
        Map<String, AttributeValue> studentItem = convertStudentToAttributeValueMap(student);

        transactItems.add(TransactWriteItem.builder()
                .put(Put.builder()
                        .tableName("Student")
                        .item(studentItem)
                        .build())
                .build());

        // Execute transaction
        TransactWriteItemsRequest request = TransactWriteItemsRequest.builder()
                .transactItems(transactItems)
                .build();

        try {
            amazonDynamoDB.transactWriteItems(request);
        } catch (TransactionCanceledException e) {
            throw new RuntimeException("Transaction failed, please try again.", e);
        }

        // Send update to clients
        //Null check prevElective
        if (prevElective != null) {
            sendElectiveCapacityUpdate(prevElective);
        }

        sendElectiveCapacityUpdate(elective);
    }


    public Map<String, AttributeValue> convertStudentToAttributeValueMap(Student student) {
        Map<String, AttributeValue> item = new HashMap<>();

        if (student.getId() != null) {
            item.put("id", AttributeValue.builder().s(student.getId()).build());
        }

        if (student.getName() != null) {
            item.put("name", AttributeValue.builder().s(student.getName()).build());
        }

        if (student.getEmail() != null) {
            item.put("email", AttributeValue.builder().s(student.getEmail()).build());
        }

        if (student.getPassword() != null) {
            item.put("password", AttributeValue.builder().s(student.getPassword()).build());
        }

        if (student.getRoles() != null) {
            item.put("roles", AttributeValue.builder()
                    .ss(student.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                    .build());
        }

        if (student.getCreatedAt() != null) {
            item.put("createdAt", AttributeValue.builder().s(student.getCreatedAt().toString()).build()); // ISO-8601 format
        }

        if (student.getDepartment() != null) {
            item.put("department", AttributeValue.builder().s(student.getDepartment().name()).build());
        }

        if (student.getContactNumber() != null) {
            item.put("contactNumber", AttributeValue.builder().n(String.valueOf(student.getContactNumber())).build());
        }

        if (student.getRollNumber() != null) {
            item.put("rollNumber", AttributeValue.builder().s(student.getRollNumber()).build());
        }

        if (student.getClassRollNumber() != null) {
            item.put("classRollNumber", AttributeValue.builder().n(String.valueOf(student.getClassRollNumber())).build());
        }

        if (student.getIsEnabled() != null) {
            item.put("isEnabled", AttributeValue.builder().bool(student.getIsEnabled()).build());
        }

        if (student.getElectiveId() != null) {
            item.put("electiveId", AttributeValue.builder().s(student.getElectiveId()).build());
        }

        return item;
    }



    /**
     * Sends an elective capacity update message to the SQS queue.
     *
     * @param elective The elective object containing the elective details.
     */
    private void sendElectiveCapacityUpdate(Elective elective) {
        // Prepare the payload to send to SQS
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("id", elective.getId());
        updatePayload.put("capacity", elective.getCapacity());
        updatePayload.put("version", elective.getVersion());

        // Convert the payload to a JSON string
        String messageBody = convertToJson(updatePayload);

        // Send the message to the SQS queue
        sendToSqs(messageBody);
    }
    /**
     * Sends the message to the SQS queue.
     *
     * @param messageBody The message content to send to the queue.
     */
    private void sendToSqs(String messageBody) {
        try {
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(electiveUpdateQueueUrl)
                    .messageBody(messageBody)
                    .messageGroupId("broadcast")
                    .build();

            sqsClient.sendMessage(sendMessageRequest);
            System.out.println("Elective capacity update sent to SQS queue successfully.");

        } catch (Exception e) {
            logger.info("Failed to send message to SQS queue: {}", e.getMessage());
        }
    }

    /**
     * Converts a Java object to a JSON string.
     *
     * @param object The object to convert.
     * @return The JSON string representation of the object.
     */
    private String convertToJson(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("Failed to send message to SQS queue: {}", e.getMessage());
            return "{}";
        }
    }



    public UserProfileResponse getProfile() {
        String mail = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(mail).orElse(null);
        if (student == null) {
            throw new RuntimeException("User not found");
        }
        Elective elective =null;
        if(student.getElectiveId() != null) {
             elective = electiveRepository.findById(student.getElectiveId()).orElse(null);
        }

        return new UserProfileResponse(
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


    }

    public void updateProfile(UserUpdateRequest request) {
        String mail = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(mail).orElseThrow(() -> new RuntimeException("User not found"));
        student.setName(request.name());
        student.setContactNumber(request.contactNumber());
        student.setRollNumber(request.rollNumber());
        student.setClassRollNumber(request.classRollNumber());
        studentRepository.update(student);
    }

    public void changePassword(Map<String, String> request) {
        String mail = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (!passwordEncoder.matches(currentPassword, student.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        student.setPassword(passwordEncoder.encode(newPassword));

        studentRepository.update(student);
    }


    public ElectiveTimeResponse getElectiveTimeSettings() {
        Settings settings = settingsRepository.findById("1").orElse(new Settings());
        return new ElectiveTimeResponse(
                settings.getElectiveOpeningTime(),
                settings.getElectiveClosingTime(),
                settings.getElectiveOpeningTime().isBefore(LocalDateTime.now().atZone(ZoneId.systemDefault()))
        );

    }
}
