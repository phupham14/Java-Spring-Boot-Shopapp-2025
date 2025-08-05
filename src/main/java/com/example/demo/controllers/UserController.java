package com.example.demo.controllers;

import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserLoginDTO;
import com.example.demo.responses.LoginResponse;
import com.example.demo.responses.RegisterResponse;
import com.example.demo.services.IUserService;
import com.example.demo.components.LocalizationUtils;
import com.example.demo.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();

                String joinedErrors = String.join("; ", errorMessages);

                return ResponseEntity.badRequest().body(RegisterResponse.builder()
                        .message(joinedErrors)
                        .build());
            }

            if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                String msg = localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH);
                return ResponseEntity.badRequest().body(RegisterResponse.builder()
                        .message(msg)
                        .build());
            }

            userService.createUser(userDTO);

            String successMessage = localizationUtils.getLocalizedMessage(MessageKeys.USER_REGISTER_SUCCESSFULLY);
            return ResponseEntity.ok(RegisterResponse.builder()
                    .message(successMessage)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(RegisterResponse.builder()
                    .message(e.getMessage())
                    .build());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            String token = userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());
            Map<String, String> response = Map.of("token", token);
            return ResponseEntity.ok(LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                            .token(token)
                    .build()); // Trả về JSON chuẩn
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED))
                            .build());
        }
    }

}
