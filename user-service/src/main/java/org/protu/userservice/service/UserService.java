package org.protu.userservice.service;

import org.protu.userservice.dto.RegisterRequestDTO;
import org.protu.userservice.dto.UserUpdateDto;
import org.protu.userservice.exceptions.ResourceNotFoundException;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService{

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User registerUser(RegisterRequestDTO registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
//        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPasswordHash(registerRequest.getPassword());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setAuthorities("ROLE_USER");

        return userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setFirstName(userUpdateDto.getFirstName());
        user.setLastName(userUpdateDto.getLastName());
        user.setPhoneNumber(userUpdateDto.getPhoneNumber());
        userRepository.save(user);
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
