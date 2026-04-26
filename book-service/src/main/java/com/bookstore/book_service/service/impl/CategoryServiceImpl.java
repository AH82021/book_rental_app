package com.bookstore.book_service.service.impl;

import com.bookstore.book_service.config.CacheConfig;
import com.bookstore.book_service.dto.CategoryCreateRequest;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.dto.CategoryUpdateRequest;
import com.bookstore.book_service.exception.DuplicateResourceException;
import com.bookstore.book_service.exception.InvalidRequestException;
import com.bookstore.book_service.exception.ResourceNotFoundException;
import com.bookstore.book_service.mapper.CategoryMapper;
import com.bookstore.book_service.model.Category;
import com.bookstore.book_service.repository.CategoryRepository;
import com.bookstore.book_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES_BY_SLUG, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORY_HIERARCHY, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        log.debug("Creating category with name: {}", request.getName());

        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        // Validate parent category if specified
        Category parent = null;
        if (request.getParentId() != null) {
            parent = findCategoryByIdOrThrow(request.getParentId());
        }

        Category category = categoryMapper.toEntity(request);
        category.setParent(parent);

        // Generate slug from name
        String slug = generateSlug(request.getName());
        category.setSlug(ensureUniqueSlug(slug));

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with ID: {} and name: {}", savedCategory.getId(), savedCategory.getName());

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Fetching category with ID: {}", id);

        Category category = findCategoryByIdOrThrow(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_CATEGORIES_BY_SLUG, key = "#slug")
    public CategoryResponse getCategoryBySlug(String slug) {
        log.debug("Fetching category with slug: {}", slug);

        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));

        return categoryMapper.toResponse(category);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES_BY_SLUG, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORY_HIERARCHY, allEntries = true)
    })
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        log.debug("Updating category with ID: {}", id);

        Category existingCategory = findCategoryByIdOrThrow(id);

        // Check if name is being changed and if new name already exists
        if (StringUtils.hasText(request.getName()) &&
                !existingCategory.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        // Validate parent change if specified
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new InvalidRequestException("Category cannot be its own parent");
            }

            if (!isValidHierarchy(id, request.getParentId())) {
                throw new InvalidRequestException("Invalid parent assignment would create a cycle in hierarchy");
            }

            Category newParent = findCategoryByIdOrThrow(request.getParentId());
            existingCategory.setParent(newParent);
        } else if (request.getParentId() == null && request.getName() != null) {
            // Explicitly setting parent to null
            existingCategory.setParent(null);
        }

        // Update basic fields
       categoryMapper.updateEntity(existingCategory,request);

        // Update slug if name changed
        if (StringUtils.hasText(request.getName()) &&
                !existingCategory.getName().equals(request.getName())) {
            String newSlug = generateSlug(request.getName());
            existingCategory.setSlug(ensureUniqueSlug(newSlug, id));
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        log.info("Updated category with ID: {}", updatedCategory.getId());

        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES_BY_SLUG, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORY_HIERARCHY, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public void deleteCategory(Long id) {
        log.debug("Deleting category with ID: {}", id);

        Category category = findCategoryByIdOrThrow(id);

        // Check if category has books
        if (!category.getBooks().isEmpty()) {
            throw new InvalidRequestException("Cannot delete category with books. Please move books to another category first.");
        }

        // Check if category has children
        if (!category.getChildren().isEmpty()) {
            throw new InvalidRequestException("Cannot delete category with subcategories. Please delete or move subcategories first.");
        }

        categoryRepository.delete(category);
        log.info("Deleted category with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.debug("Fetching all categories with pagination: {}", pageable);

        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getRootCategories(Pageable pageable) {
        log.debug("Fetching root categories");

        Page<Category> categories = categoryRepository.findByParentIsNull(pageable);
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getSubcategories(Long parentId, Pageable pageable) {
        log.debug("Fetching subcategories for parent ID: {}", parentId);

        // Validate parent exists
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Category not found with ID: " + parentId);
        }

        Page<Category> categories = categoryRepository.findByParentId(parentId, pageable);
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_CATEGORY_HIERARCHY, key = "'full'")
    public List<CategoryResponse> getCategoryHierarchy() {
        log.debug("Fetching complete category hierarchy");

        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderByName();
        return buildHierarchy(rootCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryHierarchy(Long categoryId) {
        log.debug("Fetching category hierarchy for ID: {}", categoryId);

        Category category = findCategoryByIdOrThrow(categoryId);
        return categoryMapper.toResponseWithChildren(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategoriesByName(String name, Pageable pageable) {
        log.debug("Searching categories by name: {}", name);

        Page<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name, pageable);
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategoriesWithBooks(Pageable pageable) {
        log.debug("Fetching categories with books");

        Page<Category> categories = categoryRepository.findCategoriesWithBooks(pageable);
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "#categoryId"),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES_BY_SLUG, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORY_HIERARCHY, allEntries = true)
    })
    public CategoryResponse moveCategory(Long categoryId, Long newParentId) {
        log.debug("Moving category {} to new parent {}", categoryId, newParentId);

        Category category = findCategoryByIdOrThrow(categoryId);

        if (newParentId != null) {
            if (categoryId.equals(newParentId)) {
                throw new InvalidRequestException("Category cannot be its own parent");
            }

            if (!isValidHierarchy(categoryId, newParentId)) {
                throw new InvalidRequestException("Invalid parent assignment would create a cycle in hierarchy");
            }

            Category newParent = findCategoryByIdOrThrow(newParentId);
            category.setParent(newParent);
        } else {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Moved category {} to new parent {}", categoryId, newParentId);

        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryAncestors(Long categoryId) {
        log.debug("Fetching ancestors for category ID: {}", categoryId);

        Category category = findCategoryByIdOrThrow(categoryId);
        List<CategoryResponse> ancestors = new ArrayList<>();

        Category current = category.getParent();
        while (current != null) {
            ancestors.add(categoryMapper.toResponse(current));
            current = current.getParent();
        }

        Collections.reverse(ancestors); // Return from root to immediate parent
        return ancestors;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryDescendants(Long categoryId) {
        log.debug("Fetching descendants for category ID: {}", categoryId);

        Category category = findCategoryByIdOrThrow(categoryId);
        List<CategoryResponse> descendants = new ArrayList<>();
        collectDescendants(category, descendants);

        return descendants;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_BOOK_COUNTS, key = "'cat-total'")
    public long getTotalCategoryCount() {
        return categoryRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getBookCountInCategory(Long categoryId) {
        // This would need to be implemented to count books in category and all subcategories
        // For now, return direct book count
        Category category = findCategoryByIdOrThrow(categoryId);
        return category.getBooks().size();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidHierarchy(Long categoryId, Long newParentId) {
        if (newParentId == null) {
            return true; // Making something a root category is always valid
        }

        // Check if newParentId is a descendant of categoryId
        Category newParent = findCategoryByIdOrThrow(newParentId);
        return !isDescendant(categoryId, newParent);
    }

    // Helper methods
    private Category findCategoryByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
    }

    private String ensureUniqueSlug(String baseSlug) {
        return ensureUniqueSlug(baseSlug, null);
    }

    private String ensureUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            Optional<Category> existing = categoryRepository.findBySlug(slug);
            if (existing.isEmpty() || (excludeId != null && existing.get().getId().equals(excludeId))) {
                break;
            }
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    private List<CategoryResponse> buildHierarchy(List<Category> categories) {
        return categories.stream()
                .map(categoryMapper::toResponseWithChildren)
                .collect(Collectors.toList());
    }

    private void collectDescendants(Category category, List<CategoryResponse> descendants) {
        for (Category child : category.getChildren()) {
            descendants.add(categoryMapper.toResponse(child));
            collectDescendants(child, descendants);
        }
    }

    private boolean isDescendant(Long ancestorId, Category category) {
        Category current = category.getParent();
        while (current != null) {
            if (current.getId().equals(ancestorId)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
