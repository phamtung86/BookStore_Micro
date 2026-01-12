package com.bookstore.identity.service;

import com.bookstore.common.dto.response.ServiceResponse;

public interface IUserService {
    ServiceResponse getAllUsers();

    ServiceResponse getUserById(Long id);

    ServiceResponse getMyProfile();

    ServiceResponse deleteUser(Long id);
}
