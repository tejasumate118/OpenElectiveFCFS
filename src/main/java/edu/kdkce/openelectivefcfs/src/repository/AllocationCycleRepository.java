package edu.kdkce.openelectivefcfs.src.repository;

import edu.kdkce.openelectivefcfs.src.model.AllocationCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllocationCycleRepository extends JpaRepository<AllocationCycle, Long> {

    Optional<AllocationCycle> findByCycleName(String cycleName);
}
