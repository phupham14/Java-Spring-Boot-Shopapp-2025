package com.example.demo.reposistories;

import com.example.demo.models.Category;
import com.example.demo.models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryReposistory extends JpaRepository <Category, Long> {
}
