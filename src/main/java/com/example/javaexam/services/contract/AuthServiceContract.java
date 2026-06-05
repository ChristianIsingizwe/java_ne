package com.example.javaexam.services.contract;

import com.example.javaexam.dtos.auth.AuthResponse;
import com.example.javaexam.dtos.auth.LoginRequest;
import com.example.javaexam.dtos.auth.SignupRequest;

public interface AuthServiceContract {

    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
