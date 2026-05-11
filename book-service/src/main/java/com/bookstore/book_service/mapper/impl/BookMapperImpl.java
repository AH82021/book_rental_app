package com.bookstore.book_service.mapper.impl;

import com.bookstore.book_service.dto.*;
import com.bookstore.book_service.model.*;
import com.bookstore.book_service.mapper.BookMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookMapperImpl implements BookMapper {

    @Override
    public Book toEntity(BookCreateRequest request) {
        if (request == null) return null;
        Book book = new Book();
        updateEntityFromRequest(book, request);
        return book;
    }

    @Override
    public Book toEntity(BookCreateRequest request, Set<Category> categories) {
        Book book = toEntity(request);
        if (book != null && categories != null) {
            book.setCategories(new HashSet<>(categories));
        }
        return book;
    }

    @Override
    public void updateEntity(Book book, BookUpdateRequest request) {
        if (request == null || book == null) return;
        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getPrice() != null) book.setPrice(request.getPrice());
        if (request.getRentalPrice() != null) book.setRentalPrice(request.getRentalPrice());
        if (request.getPublicationDate() != null) book.setPublicationDate(request.getPublicationDate());
        if (request.getPublisher() != null) book.setPublisher(request.getPublisher());
        if (request.getPages() != null) book.setPages(request.getPages());
        if (request.getLanguage() != null) book.setLanguage(request.getLanguage());
        if (request.getCoverImageUrl() != null) book.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getTotalCopies() != null) book.setTotalCopies(request.getTotalCopies());
        if (request.getAvailableCopies() != null) book.setAvailableCopies(request.getAvailableCopies());
    }

    @Override
    public BookResponse toResponse(Book book) {
        if (book == null) return null;
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setDescription(book.getDescription());
        response.setPrice(book.getPrice());
        response.setRentalPrice(book.getRentalPrice());
        response.setPublicationDate(book.getPublicationDate());
        response.setPublisher(book.getPublisher());
        response.setPages(book.getPages());
        response.setLanguage(book.getLanguage());
        response.setCoverImageUrl(book.getCoverImageUrl());
        response.setStatus(book.getStatus());
        response.setTotalCopies(book.getTotalCopies());
        response.setAvailableCopies(book.getAvailableCopies());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());
        response.setIsAvailable(book.getAvailableCopies() != null && book.getAvailableCopies() > 0);
        
        if (book.getCategories() != null) {
            response.setCategories(book.getCategories().stream()
                    .map(this::toCategoryResponse)
                    .collect(java.util.stream.Collectors.toSet()));
        }
        return response;
    }

    @Override
    public Set<BookResponse> toResponseSet(Set<Book> books) {
        if (books == null) return new HashSet<>();
        return books.stream().map(this::toResponse).collect(Collectors.toSet());
    }

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
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
    public Set<CategoryResponse> toCategoryResponseSet(Set<Category> categories) {
        if (categories == null) return new HashSet<>();
        return categories.stream().map(this::toCategoryResponse).collect(Collectors.toSet());
    }

    private void updateEntityFromRequest(Book book, BookCreateRequest request) {
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());
        book.setRentalPrice(request.getRentalPrice());
        book.setPublicationDate(request.getPublicationDate());
        book.setPublisher(request.getPublisher());
        book.setPages(request.getPages());
        book.setLanguage(request.getLanguage() != null ? request.getLanguage() : "English");
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getAvailableCopies());
    }
}
