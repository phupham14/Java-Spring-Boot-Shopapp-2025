package com.example.demo.reposistories;

import com.example.demo.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleReposistory extends JpaRepository<Role, Long> {

}
