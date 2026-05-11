package com.bookstore.book_service.mapper;

import java.util.Set;

public interface BookMapper {
    com.bookstore.book_service.model.Book toEntity(com.bookstore.book_service.dto.BookCreateRequest request);
    com.bookstore.book_service.model.Book toEntity(com.bookstore.book_service.dto.BookCreateRequest request, Set<com.bookstore.book_service.model.Category> categories);
    void updateEntity(com.bookstore.book_service.model.Book book, com.bookstore.book_service.dto.BookUpdateRequest request);
    com.bookstore.book_service.dto.BookResponse toResponse(com.bookstore.book_service.model.Book book);
    Set<com.bookstore.book_service.dto.BookResponse> toResponseSet(Set<com.bookstore.book_service.model.Book> books);
    com.bookstore.book_service.dto.CategoryResponse toCategoryResponse(com.bookstore.book_service.model.Category category);
    Set<com.bookstore.book_service.dto.CategoryResponse> toCategoryResponseSet(Set<com.bookstore.book_service.model.Category> categories);
}
