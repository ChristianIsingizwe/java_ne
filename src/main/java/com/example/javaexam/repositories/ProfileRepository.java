package com.example.javaexam.repositories;

import com.example.javaexam.models.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByNationalId(String nationalId);
    Optional<Profile> findByEmailIgnoreCase(String email);
    Optional<Profile> findByPhoneNumber(String phoneNumber);
    Optional<Profile> findByNationalId(String nationalId);
}
