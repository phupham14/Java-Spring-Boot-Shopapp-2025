package com.example.demo.reposistories;

import com.example.demo.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderReposistory extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
}
