package com.bookstore.book_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String parentName;
    private Integer bookCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}