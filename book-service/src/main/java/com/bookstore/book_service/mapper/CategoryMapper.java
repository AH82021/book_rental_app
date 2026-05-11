package com.bookstore.book_service.mapper;

import java.util.Set;

public interface CategoryMapper {
    com.bookstore.book_service.model.Category toEntity(com.bookstore.book_service.dto.CategoryCreateRequest request);
    com.bookstore.book_service.model.Category toEntity(com.bookstore.book_service.dto.CategoryCreateRequest request, com.bookstore.book_service.model.Category parent);
    void updateEntity(com.bookstore.book_service.model.Category category, com.bookstore.book_service.dto.CategoryUpdateRequest request);
    void updateEntity(com.bookstore.book_service.model.Category category, com.bookstore.book_service.dto.CategoryUpdateRequest request, com.bookstore.book_service.model.Category parent);
    com.bookstore.book_service.dto.CategoryResponse toResponse(com.bookstore.book_service.model.Category category);
    Set<com.bookstore.book_service.dto.CategoryResponse> toResponseSet(Set<com.bookstore.book_service.model.Category> categories);
    com.bookstore.book_service.dto.CategoryResponse toResponseWithChildren(com.bookstore.book_service.model.Category category);
}
