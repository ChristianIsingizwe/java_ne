package com.example.javaexam.repositories;

import com.example.javaexam.models.Role;
import com.example.javaexam.models.enums.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
