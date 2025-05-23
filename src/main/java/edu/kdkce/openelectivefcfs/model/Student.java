package edu.kdkce.openelectivefcfs.model;


import edu.kdkce.openelectivefcfs.converter.LocalDateTimeConverter;
import edu.kdkce.openelectivefcfs.enums.DepartmentName;
import edu.kdkce.openelectivefcfs.enums.Role;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;
import java.util.Set;

@DynamoDbBean
public class Student {

    private String id;
    private String name;
    private String email;
    private String password;
    private Set<Role> roles;
    private LocalDateTime createdAt = LocalDateTime.now();

    private DepartmentName department;
    private Long contactNumber;
    private String rollNumber;
    private Integer classRollNumber;
    private Boolean isEnabled;
    private String electiveId;




    public DepartmentName getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentName department) {
        this.department = department;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    @DynamoDbAttribute("isEnabled")
    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public String getElectiveId() {
        return electiveId;
    }

    public void setElectiveId(String electiveId) {
        this.electiveId = electiveId;
    }

    public Long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Long contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Integer getClassRollNumber() {
        return classRollNumber;
    }

    public void setClassRollNumber(Integer classRollNumber) {
        this.classRollNumber = classRollNumber;
    }
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @DynamoDbConvertedBy(LocalDateTimeConverter.class)
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
