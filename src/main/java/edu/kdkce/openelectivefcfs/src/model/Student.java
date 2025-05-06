package edu.kdkce.openelectivefcfs.src.model;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;
import jakarta.persistence.*;

@Entity
public class Student extends User {
    @Enumerated(EnumType.STRING)
    private DepartmentName department;
    private Long contactNumber;
    private String rollNumber;
    private Boolean isEnabled;
    @ManyToOne
    @JoinColumn(name = "elective_id")
    private Elective elective;

    public boolean canEnrollIn(Elective elective) {
        return !this.department.equals(elective.getDepartmentName()) // Not same department
                && !(isCSEorIT(this.department) && isCSEorIT(elective.getDepartmentName())) // No CSE-IT cross selection
                && elective.getCapacity() > 0; // Check capacity directly
    }


    private boolean isCSEorIT(DepartmentName department) {
        return department == DepartmentName.CSE || department == DepartmentName.IT;
    }

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

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Elective getElective() {
        return elective;
    }

    public void setElective(Elective elective) {
        this.elective = elective;
    }

    public Long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Long contactNumber) {
        this.contactNumber = contactNumber;
    }
}
