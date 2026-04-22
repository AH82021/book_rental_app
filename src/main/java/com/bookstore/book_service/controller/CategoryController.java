package com.bookstore.book_service.controller;

import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.service.BookService;
import com.bookstore.book_service.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management operations")
public class CategoryController{
    private final CategoryService categoryService;


    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Fetching all categories with pagination: {}", pageable);
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/roots")
    public ResponseEntity<Page<CategoryResponse>> getRootCategories(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Fetching root categories with pagination: {}", pageable);
        Page<CategoryResponse> categories = categoryService.getRootCategories(pageable);
        return ResponseEntity.ok(categories);
    }

}
