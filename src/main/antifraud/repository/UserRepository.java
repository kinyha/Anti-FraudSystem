package antifraud.repository;

import antifraud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    Boolean existsUserByRole(String role);
    Boolean findByRole(String role);
    Optional<User> findUserByUsernameIgnoreCase(String username);
}