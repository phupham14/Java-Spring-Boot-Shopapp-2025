package com.example.demo.services;

import com.example.demo.dtos.CategoryDTO;
import com.example.demo.models.Category;
import com.example.demo.reposistories.CategoryReposistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoryService implements ICategoryService{
    private final CategoryReposistory categoryReposistory;

    @Override
    public Category createCategory(CategoryDTO categoryDTO) {
        Category newCategory = Category.builder().name(categoryDTO.getName()).build();
        return categoryReposistory.save(newCategory);
    }

    @Override
    public Category getCategoryById(long id) {
        return categoryReposistory.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryReposistory.findAll();
    }

    @Override
    public Category updateCategory(long categoryId, @RequestBody CategoryDTO categoryDTO) {
        Category categoryToUpdate = getCategoryById(categoryId);
        categoryToUpdate.setName(categoryDTO.getName());
        categoryReposistory.save(categoryToUpdate);
        return categoryToUpdate;
    }

    @Override
    public void deleteCategory(long id) {
        categoryReposistory.deleteById(id);
    }
}
