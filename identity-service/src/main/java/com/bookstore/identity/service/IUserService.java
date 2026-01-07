package com.bookstore.identity.service;

import com.bookstore.identity.dto.UserDTO;
import java.util.List;

public interface IUserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO getMyProfile();
    void deleteUser(Long id);
}
