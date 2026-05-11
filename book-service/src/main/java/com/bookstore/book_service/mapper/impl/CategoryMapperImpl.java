package com.bookstore.book_service.mapper.impl;

import com.bookstore.book_service.dto.*;
import com.bookstore.book_service.model.*;
import com.bookstore.book_service.mapper.CategoryMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toEntity(CategoryCreateRequest request) {
        if (request == null) return null;
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return category;
    }

    @Override
    public Category toEntity(CategoryCreateRequest request, Category parent) {
        Category category = toEntity(request);
        if (category != null) {
            category.setParent(parent);
        }
        return category;
    }

    @Override
    public void updateEntity(Category category, CategoryUpdateRequest request) {
        if (category == null || request == null) return;
        if (request.getName() != null) category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
    }

    @Override
    public void updateEntity(Category category, CategoryUpdateRequest request, Category parent) {
        updateEntity(category, request);
        if (category != null) {
            category.setParent(parent);
        }
    }

    @Override
    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSlug(category.getSlug());
        response.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        response.setParentName(category.getParent() != null ? category.getParent().getName() : null);
        response.setBookCount(category.getBooks() != null ? category.getBooks().size() : 0);
        return response;
    }

    @Override
    public Set<CategoryResponse> toResponseSet(Set<Category> categories) {
        if (categories == null) return new HashSet<>();
        return categories.stream().map(this::toResponse).collect(Collectors.toSet());
    }

    @Override
    public CategoryResponse toResponseWithChildren(Category category) {
        CategoryResponse response = toResponse(category);
        if (response != null && category.getChildren() != null) {
            response.setChildren(category.getChildren().stream()
                    .map(this::toResponseWithChildren)
                    .collect(Collectors.toSet()));
        }
        return response;
    }
}
