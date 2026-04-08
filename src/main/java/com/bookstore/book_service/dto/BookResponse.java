package com.bookstore.book_service.dto;

import com.bookstore.book_service.model.BookStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private BigDecimal price;
    private BigDecimal rentalPrice;
    private LocalDate publicationDate;
    private String publisher;
    private Integer pages;
    private String language;
    private String coverImageUrl;
    private Integer totalCopies;
    private Integer availableCopies;
    private BookStatus status;
    private Set<CategoryResponse> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isAvailable;
}

