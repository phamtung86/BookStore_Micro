package com.bookstore.identity.service;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.identity.dto.LoginRequest;
import com.bookstore.identity.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface IAuthService {

    ServiceResponse register(RegisterRequest request);

    ServiceResponse login(LoginRequest request, HttpServletRequest httpRequest);

    ServiceResponse refreshToken(String refreshToken);
}
