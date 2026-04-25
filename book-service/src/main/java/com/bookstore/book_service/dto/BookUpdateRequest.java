package com.bookstore.book_service.dto;


import com.bookstore.book_service.model.BookStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class BookUpdateRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must be a valid monetary amount")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Rental price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Rental price must be a valid monetary amount")
    private BigDecimal rentalPrice;

    private LocalDate publicationDate;

    @Size(max = 255, message = "Publisher must not exceed 255 characters")
    private String publisher;

    @Min(value = 1, message = "Pages must be at least 1")
    private Integer pages;

    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    private String coverImageUrl;

    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    private BookStatus status;

    private Set<Long> categoryIds;
}

