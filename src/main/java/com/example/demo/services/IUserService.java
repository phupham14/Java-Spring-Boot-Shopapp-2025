package com.example.demo.services;

import com.example.demo.dtos.UserDTO;
import com.example.demo.models.User;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;

    User updateUser(Long id, UserDTO userDTO) throws Exception;

    String login(String phoneNumber, String password) throws Exception;

    User getUserDetailsFromToken(String token) throws Exception;
}
