package com.example.javaexam.services.contract;

import com.example.javaexam.dtos.common.StatusUpdateRequest;
import com.example.javaexam.dtos.user.CreateUserRequest;
import com.example.javaexam.dtos.user.UserResponse;
import java.util.List;

public interface UserServiceContract {

    UserResponse create(CreateUserRequest request);

    List<UserResponse> list();

    UserResponse me(String email);

    UserResponse updateStatus(Long userId, StatusUpdateRequest request);
}
