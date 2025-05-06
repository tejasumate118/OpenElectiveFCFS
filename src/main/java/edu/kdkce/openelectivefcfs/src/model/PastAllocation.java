package edu.kdkce.openelectivefcfs.src.model;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;
import jakarta.persistence.*;

@Entity
public class PastAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String studentName;
    private String rollNumber;
    private Long contactNumber;
    private String mailId;
    private DepartmentName studentDepartment;

    private String electiveName;
    private DepartmentName electiveDepartment;
    @ManyToOne
    @JoinColumn(name = "allocation_cycle_id")
    private AllocationCycle allocationCycle;


    //getter and setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public Long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Long contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    public DepartmentName getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(DepartmentName studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    public String getElectiveName() {
        return electiveName;
    }

    public void setElectiveName(String electiveName) {
        this.electiveName = electiveName;
    }

    public DepartmentName getElectiveDepartment() {
        return electiveDepartment;
    }

    public void setElectiveDepartment(DepartmentName electiveDepartment) {
        this.electiveDepartment = electiveDepartment;
    }

    public AllocationCycle getAllocationCycle() {
        return allocationCycle;
    }

    public void setAllocationCycle(AllocationCycle allocationCycle) {
        this.allocationCycle = allocationCycle;
    }
}
