package com.bookstore.identity.service;

import com.bookstore.identity.dto.LoginRequest;
import com.bookstore.identity.dto.LoginResponse;
import com.bookstore.identity.dto.RegisterRequest;
import com.bookstore.identity.dto.UserDTO;

public interface IAuthService {

    UserDTO register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(String refreshToken);
}
