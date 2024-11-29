package org.protu.userservice.service;

import org.protu.userservice.dto.RegisterRequestDTO;
import org.protu.userservice.dto.UserUpdateDto;
import org.protu.userservice.model.User;
public interface IUserService {
    User registerUser(RegisterRequestDTO registerRequest);
    void deactivateUser(Long userId);
    User updateUser(Long userId, UserUpdateDto userUpdateDto);
    User getUserById(Long userId);
}
