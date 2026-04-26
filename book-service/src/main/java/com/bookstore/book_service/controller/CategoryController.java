package com.bookstore.book_service.controller;

import com.bookstore.book_service.dto.CategoryCreateRequest;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.dto.CategoryUpdateRequest;
import com.bookstore.book_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management operations")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new category with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Category with name already exists")
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        log.info("Creating new category with name: {}", request.getName());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.debug("Fetching category with ID: {}", id);
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieves a category by its slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        log.debug("Fetching category with slug: {}", slug);
        CategoryResponse response = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category with name already exists")
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        log.info("Updating category with ID: {}", id);
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category if it has no books or subcategories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Category has books or subcategories"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.info("Deleting category with ID: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves all categories with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Fetching all categories with pagination: {}", pageable);
        Page<CategoryResponse> response = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Retrieves categories that have no parent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Root categories retrieved successfully")
    })
    public ResponseEntity<Page<CategoryResponse>> getRootCategories(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Fetching root categories");
        Page<CategoryResponse> response = categoryService.getRootCategories(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{parentId}/subcategories")
    @Operation(summary = "Get subcategories", description = "Retrieves subcategories of a parent category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subcategories retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parent category not found")
    })
    public ResponseEntity<Page<CategoryResponse>> getSubcategories(
            @Parameter(description = "Parent category ID") @PathVariable Long parentId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Fetching subcategories for parent ID: {}", parentId);
        Page<CategoryResponse> response = categoryService.getSubcategories(parentId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hierarchy")
    @Operation(summary = "Get category hierarchy", description = "Retrieves the complete category hierarchy starting from root categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category hierarchy retrieved successfully")
    })
    public ResponseEntity<List<CategoryResponse>> getCategoryHierarchy() {
        log.debug("Fetching complete category hierarchy");
        List<CategoryResponse> response = categoryService.getCategoryHierarchy();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/hierarchy")
    @Operation(summary = "Get category hierarchy", description = "Retrieves the category hierarchy starting from a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category hierarchy retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryHierarchy(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.debug("Fetching category hierarchy for ID: {}", id);
        CategoryResponse response = categoryService.getCategoryHierarchy(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Searches categories by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<CategoryResponse>> searchCategoriesByName(
            @Parameter(description = "Category name") @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Searching categories with name: {}", name);
        Page<CategoryResponse> response = categoryService.searchCategoriesByName(name, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/with-books")
    @Operation(summary = "Get categories with books", description = "Retrieves categories that have books associated with them")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories with books retrieved successfully")
    })
    public ResponseEntity<Page<CategoryResponse>> getCategoriesWithBooks(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.debug("Fetching categories with books");
        Page<CategoryResponse> response = categoryService.getCategoriesWithBooks(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/move")
    @Operation(summary = "Move category", description = "Moves a category to a new parent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category moved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parent assignment"),
            @ApiResponse(responseCode = "404", description = "Category or parent not found")
    })
    public ResponseEntity<CategoryResponse> moveCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Parameter(description = "New parent category ID (null for root)") @RequestParam(required = false) Long newParentId) {
        log.info("Moving category {} to new parent {}", id, newParentId);
        CategoryResponse response = categoryService.moveCategory(id, newParentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/ancestors")
    @Operation(summary = "Get category ancestors", description = "Retrieves all ancestors (parents) of a category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ancestors retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<List<CategoryResponse>> getCategoryAncestors(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.debug("Fetching ancestors for category ID: {}", id);
        List<CategoryResponse> response = categoryService.getCategoryAncestors(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/descendants")
    @Operation(summary = "Get category descendants", description = "Retrieves all descendants (children) of a category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Descendants retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<List<CategoryResponse>> getCategoryDescendants(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.debug("Fetching descendants for category ID: {}", id);
        List<CategoryResponse> response = categoryService.getCategoryDescendants(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/name/{name}")
    @Operation(summary = "Check if category exists by name", description = "Checks if a category with the given name exists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed successfully")
    })
    public ResponseEntity<Boolean> existsByName(
            @Parameter(description = "Category name") @PathVariable String name) {
        log.debug("Checking if category exists with name: {}", name);
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/slug/{slug}")
    @Operation(summary = "Check if category exists by slug", description = "Checks if a category with the given slug exists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed successfully")
    })
    public ResponseEntity<Boolean> existsBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        log.debug("Checking if category exists with slug: {}", slug);
        boolean exists = categoryService.existsBySlug(slug);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/count")
    @Operation(summary = "Get total category count", description = "Retrieves the total count of categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> getTotalCategoryCount() {
        log.debug("Fetching total category count");
        long count = categoryService.getTotalCategoryCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/book-count")
    @Operation(summary = "Get book count in category", description = "Retrieves the count of books in a category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Long> getBookCountInCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.debug("Fetching book count for category ID: {}", id);
        long count = categoryService.getBookCountInCategory(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/validate-hierarchy")
    @Operation(summary = "Validate category hierarchy", description = "Validates if a parent assignment would create a valid hierarchy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed successfully")
    })
    public ResponseEntity<Boolean> isValidHierarchy(
            @Parameter(description = "Category ID") @RequestParam Long categoryId,
            @Parameter(description = "Proposed parent category ID") @RequestParam(required = false) Long newParentId) {
        log.debug("Validating hierarchy for category {} with parent {}", categoryId, newParentId);
        boolean isValid = categoryService.isValidHierarchy(categoryId, newParentId);
        return ResponseEntity.ok(isValid);
    }
}
