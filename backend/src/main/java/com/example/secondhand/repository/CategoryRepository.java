package com.example.secondhand.repository;

import com.example.secondhand.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNull();

    boolean existsByParentId(Long parentId);
}