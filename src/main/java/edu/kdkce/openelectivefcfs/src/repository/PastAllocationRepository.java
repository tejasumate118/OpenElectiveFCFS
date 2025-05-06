package edu.kdkce.openelectivefcfs.src.repository;

import edu.kdkce.openelectivefcfs.src.model.AllocationCycle;
import edu.kdkce.openelectivefcfs.src.model.PastAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastAllocationRepository extends JpaRepository<PastAllocation, Long> {
    @Query("SELECT DISTINCT p.allocationCycle FROM PastAllocation p")
    List<String> findAllDistinctCycleNames();

    List<PastAllocation> findByAllocationCycle(AllocationCycle allocationCycle);
}
