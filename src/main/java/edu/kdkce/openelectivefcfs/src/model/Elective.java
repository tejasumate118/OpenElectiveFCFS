package edu.kdkce.openelectivefcfs.src.model;

import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Elective {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    private DepartmentName departmentName;
    private Integer maxCapacity;
    private Integer capacity;
    private String description;
    @OneToMany(mappedBy = "elective")
    private Set<Student> enrolledStudents = new HashSet<>();

    public boolean isFull() {
        return enrolledStudents.size() >= maxCapacity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DepartmentName getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(DepartmentName departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Student> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(Set<Student> enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }
}
