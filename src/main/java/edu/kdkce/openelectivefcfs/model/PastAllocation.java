package edu.kdkce.openelectivefcfs.model;

import edu.kdkce.openelectivefcfs.enums.DepartmentName;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Represents a past allocation of an elective to a student.
 * This class contains the details of the student, the elective, and the allocation cycle.
 */
@DynamoDbBean
public class PastAllocation {
    private String id;
    private String studentName;
    private String rollNumber;
    private Long contactNumber;
    private String mailId;
    private DepartmentName studentDepartment;

    private String electiveName;
    private DepartmentName electiveDepartment;
    private String allocationCycleId;



    //getter and setters

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @DynamoDbAttribute("studentName")
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @DynamoDbAttribute("rollNumber")
    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
    @DynamoDbAttribute("contactNumber")
    public Long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Long contactNumber) {
        this.contactNumber = contactNumber;
    }
    @DynamoDbAttribute("mailId")
    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }
    @DynamoDbAttribute("studentDepartment")
    public DepartmentName getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(DepartmentName studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    @DynamoDbAttribute("electiveName")
    public String getElectiveName() {
        return electiveName;
    }

    public void setElectiveName(String electiveName) {
        this.electiveName = electiveName;
    }

    @DynamoDbAttribute("electiveDepartment")
    public DepartmentName getElectiveDepartment() {
        return electiveDepartment;
    }

    public void setElectiveDepartment(DepartmentName electiveDepartment) {
        this.electiveDepartment = electiveDepartment;
    }

    @DynamoDbAttribute("allocationCycleId")
    public String getAllocationCycleId() {
        return allocationCycleId;
    }

    public void setAllocationCycleId(String allocationCycleId) {
        this.allocationCycleId = allocationCycleId;
    }
}
