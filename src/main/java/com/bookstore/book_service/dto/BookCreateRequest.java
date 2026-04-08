package com.bookstore.book_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class BookCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    @Pattern(regexp = "^\\d{10}(\\d{3})?$", message = "ISBN must be 10 or 13 digits")
    private String isbn;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
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
    private String language = "English";

    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    private String coverImageUrl;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    private Set<Long> categoryIds;
}


