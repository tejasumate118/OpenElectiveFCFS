package edu.kdkce.openelectivefcfs.src.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AllocationCycle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String cycleName;

    public AllocationCycle() {
    }

    public AllocationCycle(String cycleName) {
        this.cycleName = cycleName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCycleName() {
        return cycleName;
    }

    public void setCycleName(String cycleName) {
        this.cycleName = cycleName;
    }
}
