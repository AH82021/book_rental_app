package com.bookstore.book_service.service;


import com.bookstore.book_service.dto.CategoryCreateRequest;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.dto.CategoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing categories.
 * Provides business logic for category operations including CRUD and hierarchy management.
 */
public interface CategoryService {

    /**
     * Create a new category
     * @param request the category creation request
     * @return the created category response
     */
    CategoryResponse createCategory(CategoryCreateRequest request);

    /**
     * Get a category by ID
     * @param id the category ID
     * @return the category response
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * Get a category by slug
     * @param slug the category slug
     * @return the category response
     */
    CategoryResponse getCategoryBySlug(String slug);

    /**
     * Update an existing category
     * @param id the category ID
     * @param request the category update request
     * @return the updated category response
     */
    CategoryResponse updateCategory(Long id, CategoryUpdateRequest request);

    /**
     * Delete a category
     * @param id the category ID
     */
    void deleteCategory(Long id);

    /**
     * Get all categories with pagination
     * @param pageable pagination information
     * @return page of category responses
     */
    Page<CategoryResponse> getAllCategories(Pageable pageable);

    /**
     * Get all root categories (categories without parent)
     * @param pageable pagination information
     * @return page of root category responses
     */
    Page<CategoryResponse> getRootCategories(Pageable pageable);

    /**
     * Get subcategories of a parent category
     * @param parentId the parent category ID
     * @param pageable pagination information
     * @return page of subcategory responses
     */
    Page<CategoryResponse> getSubcategories(Long parentId, Pageable pageable);

    /**
     * Get category hierarchy starting from root categories
     * @return list of category responses with children
     */
    List<CategoryResponse> getCategoryHierarchy();

    /**
     * Get category hierarchy starting from a specific category
     * @param categoryId the root category ID for the hierarchy
     * @return category response with children
     */
    CategoryResponse getCategoryHierarchy(Long categoryId);

    /**
     * Search categories by name
     * @param name the category name (partial match)
     * @param pageable pagination information
     * @return page of category responses
     */
    Page<CategoryResponse> searchCategoriesByName(String name, Pageable pageable);

    /**
     * Get categories that have books
     * @param pageable pagination information
     * @return page of category responses with books
     */
    Page<CategoryResponse> getCategoriesWithBooks(Pageable pageable);

    /**
     * Move a category to a new parent
     * @param categoryId the category ID to move
     * @param newParentId the new parent category ID (null for root)
     * @return the updated category response
     */
    CategoryResponse moveCategory(Long categoryId, Long newParentId);

    /**
     * Get all ancestors of a category (parent, grandparent, etc.)
     * @param categoryId the category ID
     * @return list of ancestor category responses
     */
    List<CategoryResponse> getCategoryAncestors(Long categoryId);

    /**
     * Get all descendants of a category (children, grandchildren, etc.)
     * @param categoryId the category ID
     * @return list of descendant category responses
     */
    List<CategoryResponse> getCategoryDescendants(Long categoryId);

    /**
     * Check if a category exists by name
     * @param name the category name
     * @return true if category exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Check if a category exists by slug
     * @param slug the category slug
     * @return true if category exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Get total count of categories
     * @return total count of categories
     */
    long getTotalCategoryCount();

    /**
     * Get count of books in a category (including subcategories)
     * @param categoryId the category ID
     * @return count of books in the category and its subcategories
     */
    long getBookCountInCategory(Long categoryId);

    /**
     * Validate category hierarchy (check for cycles)
     * @param categoryId the category ID
     * @param newParentId the proposed parent ID
     * @return true if hierarchy is valid, false otherwise
     */
    boolean isValidHierarchy(Long categoryId, Long newParentId);
}
