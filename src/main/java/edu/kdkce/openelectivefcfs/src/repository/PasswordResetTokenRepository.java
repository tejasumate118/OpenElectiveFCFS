package edu.kdkce.openelectivefcfs.src.repository;

import edu.kdkce.openelectivefcfs.src.model.PasswordResetToken;
import edu.kdkce.openelectivefcfs.src.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    void deleteAllByUser(User user);

    Optional<PasswordResetToken> findByToken(String token);
}
