package com.edu.service;


import com.edu.domain.dto.request.AuthRequest;
import com.edu.domain.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse.TokenPair register(AuthRequest.Register request);

    AuthResponse.TokenPair login(AuthRequest.Login request);

    AuthResponse.TokenPair refresh(AuthRequest.Refresh request);

    void logout(String username);
}