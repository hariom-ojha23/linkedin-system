package hariom.linkedin.userservice.repository;

import hariom.linkedin.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> getUserByEmail(String email);
}
