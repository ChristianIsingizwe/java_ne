package com.example.javaexam.repositories;

import com.example.javaexam.models.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"profile", "roles"})
    Optional<User> findByProfileEmailIgnoreCase(String email);

    default Optional<User> findByEmailIgnoreCase(String email) {
        return findByProfileEmailIgnoreCase(email);
    }

    @EntityGraph(attributePaths = {"profile", "roles"})
    List<User> findAllByOrderByIdAsc();

    boolean existsByProfileEmailIgnoreCase(String email);

    default boolean existsByEmailIgnoreCase(String email) {
        return existsByProfileEmailIgnoreCase(email);
    }
}
