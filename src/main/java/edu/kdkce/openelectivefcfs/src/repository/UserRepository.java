package edu.kdkce.openelectivefcfs.src.repository;

import edu.kdkce.openelectivefcfs.src.model.Student;
import edu.kdkce.openelectivefcfs.src.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Student s")
    void deleteAllStudents();

    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.elective = null")
    void resetStudentElectives();

    @Query("SELECT s FROM Student s WHERE s.elective IS NOT NULL")
    List<Student> findAllAllocatedStudents();
}