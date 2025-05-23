package edu.kdkce.openelectivefcfs.src.model;
import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Set;

/**
 * Represents an elective course in the system.
 * This class is used to store and manage information about elective courses.
 */
@DynamoDbBean
public class Elective {

    private String id;
    private String name;
    private DepartmentName departmentName;
    private Integer maxCapacity;
    private Integer capacity;
    private String description;
    private Set<String> enrolledStudentIds;
    private Set<DepartmentName> allowedDepartments;
    private Long version;

    /**
     * Default constructor.
     */
    public Elective() {

    }



    @DynamoDbIgnore
    public boolean isFull() {
        return enrolledStudentIds.size() >= maxCapacity;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDbAttribute("departmentName")
    public DepartmentName getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(DepartmentName departmentName) {
        this.departmentName = departmentName;
    }

    @DynamoDbAttribute("maxCapacity")
    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @DynamoDbAttribute("capacity")
    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    @DynamoDbAttribute("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    @DynamoDbAttribute("enrolledStudentIds")
    public Set<String> getEnrolledStudentIds() {
        return enrolledStudentIds;
    }

    public void setEnrolledStudentIds(Set<String> enrolledStudentIds) {
        this.enrolledStudentIds = enrolledStudentIds;
    }
    @DynamoDbAttribute("version")
    @DynamoDbVersionAttribute

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
    @DynamoDbAttribute("allowedDepartments")
    public Set<DepartmentName> getAllowedDepartments() {
        return allowedDepartments;
    }
    public void setAllowedDepartments(Set<DepartmentName> allowedDepartments) {
        this.allowedDepartments = allowedDepartments;
    }
}
