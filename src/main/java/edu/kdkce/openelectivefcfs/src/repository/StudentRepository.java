package edu.kdkce.openelectivefcfs.src.repository;

import edu.kdkce.openelectivefcfs.src.model.Student;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepository {
    private final DynamoDbTable<Student> table;

    public StudentRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("Student", TableSchema.fromBean(Student.class));
    }

    public List<Student> findAll() {
        return table.scan().items().stream().toList();
    }

    public List<Student> findAllByElectiveIdIsNotNull() {
        return table.scan().items().stream()
                .filter(student -> student.getElectiveId() != null)
                .toList();
    }

    public void deleteAll() {
        table.scan().items().forEach(table::deleteItem);
    }

    public Optional<Student> findByEmail(String email) {
        //Log for debugging
        System.out.println("Finding student with email: " + email);
        Student item = table.scan().items().stream()
                .filter(student -> student.getEmail().equals(email))
                .findFirst()
                .orElse(null);
        //Log for debugging
        System.out.println("Found student: " + item);
        return Optional.ofNullable(item);
    }

    public Optional<Student> findById(String userId) {
        Student item = table.getItem(r -> r.key(k -> k.partitionValue(userId)));
        return Optional.ofNullable(item);

    }

    public void save(Student student) {
        try {
            PutItemEnhancedRequest<Student> request = PutItemEnhancedRequest.builder(Student.class)
                    .item(student)
                    .conditionExpression(Expression.builder()
                            .expression("attribute_not_exists(email)")
                            .build())
                    .build();

            table.putItem(request);
        } catch (ConditionalCheckFailedException e) {
            throw new RuntimeException("A student with this email already exists!");
        }
    }

    public boolean existsById(String userId) {
        return table.getItem(r -> r.key(k -> k.partitionValue(userId))) != null;
    }

    public void deleteById(String userId) {
        Student student = table.getItem(r -> r.key(k -> k.partitionValue(userId)));
        if(student == null) {
            System.out.println("Student with ID: " + userId + " not found.");
            return;
        }
        if (student != null) {
            System.out.println("Deleting student with ID: " + userId);
            table.deleteItem(student);
        }
    }

    public void update(Student student) {
        table.updateItem(student);
    }
}