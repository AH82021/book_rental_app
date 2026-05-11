package com.bookstore.book_service;

import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.BookUpdateRequest;
import com.bookstore.book_service.dto.CategoryCreateRequest;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.model.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TestFixtures {

    // ================== Book Fixtures ==================

    public static BookCreateRequest createValidBookRequest() {
        return createBookRequest(
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                "9780743273565",
                "A classic novel",
                new BigDecimal("15.99"),
                new BigDecimal("5.99"),
                100,
                100
        );
    }

    public static BookCreateRequest createBookRequest(String title, String author, String isbn,
                                                       String description, BigDecimal price,
                                                       BigDecimal rentalPrice, Integer totalCopies,
                                                       Integer availableCopies) {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle(title);
        request.setAuthor(author);
        request.setIsbn(isbn);
        request.setDescription(description);
        request.setPrice(price);
        request.setRentalPrice(rentalPrice);
        request.setTotalCopies(totalCopies);
        request.setAvailableCopies(availableCopies);
        request.setPublisher("Test Publisher");
        request.setPages(320);
        request.setLanguage("English");
        request.setPublicationDate(LocalDate.of(2020, 1, 1));
        request.setCoverImageUrl("http://example.com/cover.jpg");
        return request;
    }

    public static BookUpdateRequest createValidBookUpdateRequest() {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("Updated Title");
        request.setAuthor("Updated Author");
        request.setPrice(new BigDecimal("19.99"));
        request.setTotalCopies(150);
        request.setAvailableCopies(150);
        return request;
    }

    public static Book createValidBook() {
        return Book.builder()
                .id(1L)
                .title("The Great Gatsby")
                .author("F. Scott Fitzgerald")
                .isbn("9780743273565")
                .description("A classic novel")
                .price(new BigDecimal("15.99"))
                .rentalPrice(new BigDecimal("5.99"))
                .totalCopies(100)
                .availableCopies(100)
                .publisher("Penguin")
                .pages(320)
                .language("English")
                .publicationDate(LocalDate.of(2020, 1, 1))
                .coverImageUrl("http://example.com/cover.jpg")
                .status(BookStatus.AVAILABLE)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Book createBook(Long id, String title, String author, String isbn,
                                   Integer totalCopies, Integer availableCopies) {
        return Book.builder()
                .id(id)
                .title(title)
                .author(author)
                .isbn(isbn)
                .price(new BigDecimal("15.99"))
                .rentalPrice(new BigDecimal("5.99"))
                .totalCopies(totalCopies)
                .availableCopies(availableCopies)
                .status(BookStatus.AVAILABLE)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static BookResponse createValidBookResponse() {
        return BookResponse.builder()
                .id(1L)
                .title("The Great Gatsby")
                .author("F. Scott Fitzgerald")
                .isbn("9780743273565")
                .description("A classic novel")
                .price(new BigDecimal("15.99"))
                .rentalPrice(new BigDecimal("5.99"))
                .totalCopies(100)
                .availableCopies(100)
                .publisher("Penguin")
                .pages(320)
                .language("English")
                .publicationDate(LocalDate.of(2020, 1, 1))
                .coverImageUrl("http://example.com/cover.jpg")
                .status(BookStatus.AVAILABLE)
                .isAvailable(true)
                .categories(Collections.emptySet())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ================== Category Fixtures ==================

    public static CategoryCreateRequest createValidCategoryRequest() {
        return createCategoryRequest("Fiction", "Fiction books");
    }

    public static CategoryCreateRequest createCategoryRequest(String name, String description) {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }

    public static Category createValidCategory() {
        return Category.builder()
                .id(1L)
                .name("Fiction")
                .slug("fiction")
                .description("Fiction books")
                .parent(null)
                .children(new HashSet<>())
                .books(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Category createCategory(Long id, String name, String slug) {
        return Category.builder()
                .id(id)
                .name(name)
                .slug(slug)
                .description(name + " category")
                .parent(null)
                .children(new HashSet<>())
                .books(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Category createCategoryWithParent(Long id, String name, String slug, Category parent) {
        return Category.builder()
                .id(id)
                .name(name)
                .slug(slug)
                .description(name + " category")
                .parent(parent)
                .children(new HashSet<>())
                .books(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static CategoryResponse createValidCategoryResponse() {
        return CategoryResponse.builder()
                .id(1L)
                .name("Fiction")
                .slug("fiction")
                .description("Fiction books")
                .build();
    }

    // ================== Utility Methods ==================

    public static Set<Long> createCategoryIds(Long... ids) {
        return new HashSet<>(Set.of(ids));
    }
}

