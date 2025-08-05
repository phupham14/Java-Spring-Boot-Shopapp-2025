package com.example.demo.services;

import com.example.demo.models.Role;
import com.example.demo.reposistories.RoleReposistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final RoleReposistory roleReposistory;
    @Override
    public List<Role> getAllRoles() {
        return roleReposistory.findAll(); // Trả về tất cả roles từ DB
    }
}
