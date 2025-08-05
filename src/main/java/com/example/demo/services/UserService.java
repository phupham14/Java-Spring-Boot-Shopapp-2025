package com.example.demo.services;

import com.example.demo.components.JwtTokenUtils;
import com.example.demo.components.LocalizationUtils;
import com.example.demo.dtos.UserDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.PermissionDeniedException;
import com.example.demo.models.Role;
import com.example.demo.models.User;
import com.example.demo.reposistories.RoleReposistory;
import com.example.demo.reposistories.UserReposistory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserReposistory userReposistory;
    private final RoleReposistory roleReposistory;
    private final LocalizationUtils localizationUtils;

    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        if (userReposistory.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("User already exists");
        }
        Role role = roleReposistory.findById(userDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        if (role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new PermissionDeniedException("You don't have permission to create admin user. Please contact admin to create new user.");
        }
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookId(userDTO.getFacebookId())
                .googleId(userDTO.getGoogleId())
                .build();
        newUser.setRole(role);
        if (userDTO.getFacebookId() == 0 && userDTO.getGoogleId() == 0) {
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        }
        return userReposistory.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) throws Exception {
        Optional<User> optionalUser = userReposistory.findByPhoneNumber(phoneNumber);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("Invalid phone number or password");
        }

        User existingUser = optionalUser.get();

        if (existingUser.getFacebookId() == 0 && existingUser.getGoogleId() == 0) {
            // So sánh mật khẩu đúng cách
            if (!passwordEncoder.matches(password, existingUser.getPassword())) {
                throw new DataNotFoundException("Invalid phone number or password");
            }
        }

        Optional<Role> optionalRole = roleReposistory.findById(existingUser.getRole().getId());
        if (optionalRole.isPresent() && Role.ADMIN.equalsIgnoreCase(optionalRole.get().getName())) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage("error.login.invalid"));
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(phoneNumber, password, existingUser.getAuthorities());

        // Authenticate user (sẽ check thêm credential trong SecurityConfig)
        authenticationManager.authenticate(authenticationToken);

        return jwtTokenUtil.generateToken(existingUser);
    }

}
