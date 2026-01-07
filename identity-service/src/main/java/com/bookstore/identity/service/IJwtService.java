package com.bookstore.identity.service;

import java.util.Set;

public interface IJwtService {

    String generateToken(String username, String role, Long userId);

    String generateAccessToken(String username, String role, Long userId, Set<String> permissions);

    String extractUsername(String token);

    String generateRefreshToken(String username);

    Long getAccessTokenExpirationInSeconds();

    String getTokenType(String token);

    Boolean validateToken(String token, String username);
}
