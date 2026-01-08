package com.bookstore.identity.service;

import com.bookstore.identity.dto.LoginRequest;
import com.bookstore.identity.dto.LoginResponse;
import com.bookstore.identity.dto.RegisterRequest;
import com.bookstore.identity.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface IAuthService {

    UserDTO register(RegisterRequest request);

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    LoginResponse refreshToken(String refreshToken);
}
