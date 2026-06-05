package com.example.javaexam.repositories;

import com.example.javaexam.models.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
}
