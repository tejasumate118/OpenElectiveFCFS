package edu.kdkce.openelectivefcfs.src.repository;

import edu.kdkce.openelectivefcfs.src.model.Elective;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ElectiveRepository extends JpaRepository<Elective, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") // 5 seconds timeout
    })
    @Query("SELECT e FROM Elective e WHERE e.id = :id")
    Optional<Elective> findByIdWithLock(@Param("id") Integer id);





}
