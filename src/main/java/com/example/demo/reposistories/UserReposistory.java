package com.example.demo.reposistories;

import com.example.demo.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReposistory extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumber(String phoneNumber);
}
