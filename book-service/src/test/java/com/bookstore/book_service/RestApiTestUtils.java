package com.bookstore.book_service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.math.BigDecimal;

/**
 * Utility class for REST API testing using RestAssured.
 * Provides helper methods for common HTTP operations and assertions.
 */
public class RestApiTestUtils {

    private static final String BASE_PATH = "/api/v1";

    static {
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = BASE_PATH;
    }

    /**
     * Get default request specification with JSON content type
     */
    public static RequestSpecification defaultSpec() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    /**
     * Create a book via REST API
     */
    public static Response createBook(String title, String author, String isbn, BigDecimal price) {
        BookApiRequest request = new BookApiRequest(title, author, isbn, price);
        return defaultSpec()
                .body(request)
                .post("/books");
    }

    /**
     * Get book by ID via REST API
     */
    public static Response getBook(Long id) {
        return defaultSpec()
                .get("/books/{id}", id);
    }

    /**
     * Update book via REST API
     */
    public static Response updateBook(Long id, String title, BigDecimal price) {
        BookUpdateApiRequest request = new BookUpdateApiRequest(title, price);
        return defaultSpec()
                .body(request)
                .put("/books/{id}", id);
    }

    /**
     * Delete book via REST API
     */
    public static Response deleteBook(Long id) {
        return defaultSpec()
                .delete("/books/{id}", id);
    }

    /**
     * Create a category via REST API
     */
    public static Response createCategory(String name, String slug, String description) {
        CategoryApiRequest request = new CategoryApiRequest(name, slug, description);
        return defaultSpec()
                .body(request)
                .post("/categories");
    }

    /**
     * Get category by ID via REST API
     */
    public static Response getCategory(Long id) {
        return defaultSpec()
                .get("/categories/{id}", id);
    }

    /**
     * Get all books with pagination
     */
    public static Response getAllBooks(int page, int size) {
        return defaultSpec()
                .queryParam("page", page)
                .queryParam("size", size)
                .get("/books");
    }

    /**
     * Search books by keyword
     */
    public static Response searchBooks(String keyword) {
        return defaultSpec()
                .queryParam("keyword", keyword)
                .get("/books/search");
    }

    // ==================== Request/Response DTOs ====================

    public static class BookApiRequest {
        public String title;
        public String author;
        public String isbn;
        public BigDecimal price;
        public BigDecimal rentalPrice;
        public Integer totalCopies;
        public Integer availableCopies;

        public BookApiRequest(String title, String author, String isbn, BigDecimal price) {
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.price = price;
            this.rentalPrice = new BigDecimal("5.99");
            this.totalCopies = 100;
            this.availableCopies = 100;
        }
    }

    public static class BookUpdateApiRequest {
        public String title;
        public BigDecimal price;

        public BookUpdateApiRequest(String title, BigDecimal price) {
            this.title = title;
            this.price = price;
        }
    }

    public static class CategoryApiRequest {
        public String name;
        public String slug;
        public String description;

        public CategoryApiRequest(String name, String slug, String description) {
            this.name = name;
            this.slug = slug;
            this.description = description;
        }
    }
}

