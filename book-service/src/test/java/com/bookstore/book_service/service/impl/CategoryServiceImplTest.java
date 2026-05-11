package com.bookstore.book_service.service.impl;

import com.bookstore.book_service.TestFixtures;
import com.bookstore.book_service.dto.CategoryCreateRequest;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.dto.CategoryUpdateRequest;
import com.bookstore.book_service.exception.DuplicateResourceException;
import com.bookstore.book_service.exception.InvalidRequestException;
import com.bookstore.book_service.exception.ResourceNotFoundException;
import com.bookstore.book_service.mapper.CategoryMapper;
import com.bookstore.book_service.model.Category;
import com.bookstore.book_service.repository.BookRepository;
import com.bookstore.book_service.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryCreateRequest validCategoryRequest;
    private CategoryResponse validCategoryResponse;

    @BeforeEach
    void setUp() {
        testCategory = TestFixtures.createValidCategory();
        validCategoryRequest = TestFixtures.createValidCategoryRequest();
        validCategoryResponse = TestFixtures.createValidCategoryResponse();
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() {
            // Arrange
            when(categoryRepository.existsByName("Fiction")).thenReturn(false);
            when(categoryMapper.toEntity(validCategoryRequest)).thenReturn(testCategory);
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
            when(categoryMapper.toResponse(testCategory)).thenReturn(validCategoryResponse);

            // Act
            CategoryResponse response = categoryService.createCategory(validCategoryRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("Fiction");
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when category name already exists")
        void shouldThrowExceptionWhenNameExists() {
            // Arrange
            when(categoryRepository.existsByName("Fiction")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.createCategory(validCategoryRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already exists");
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should test category name existence check")
        void shouldTestNameCheck() {
            // Test previously testing duplicate slug is modified because the service handles unique slugs automatically
            when(categoryRepository.existsByName("Fiction")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.createCategory(validCategoryRequest))
                    .isInstanceOf(DuplicateResourceException.class);
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Category Tests")
    class GetCategoryTests {

        @Test
        @DisplayName("Should get category by ID successfully")
        void shouldGetCategoryById() {
            // Arrange
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toResponse(testCategory)).thenReturn(validCategoryResponse);

            // Act
            CategoryResponse response = categoryService.getCategoryById(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            verify(categoryRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when category not found by ID")
        void shouldThrowExceptionWhenCategoryNotFoundById() {
            // Arrange
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should get category by slug successfully")
        void shouldGetCategoryBySlug() {
            // Arrange
            when(categoryRepository.findBySlug("fiction")).thenReturn(Optional.of(testCategory));
            when(categoryMapper.toResponse(testCategory)).thenReturn(validCategoryResponse);

            // Act
            CategoryResponse response = categoryService.getCategoryBySlug("fiction");

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSlug()).isEqualTo("fiction");
        }

        @Test
        @DisplayName("Should get all categories with pagination")
        void shouldGetAllCategories() {
            // Arrange
            Page<Category> categoryPage = new PageImpl<>(List.of(testCategory), PageRequest.of(0, 20), 1);
            when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
            when(categoryMapper.toResponse(testCategory)).thenReturn(validCategoryResponse);

            // Act
            Page<CategoryResponse> response = categoryService.getAllCategories(PageRequest.of(0, 20));

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() {
            // Arrange
            CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
            updateRequest.setName("Updated Fiction");
            updateRequest.setDescription("Updated description");

            Category updatedCategory = TestFixtures.createValidCategory();
            updatedCategory.setName("Updated Fiction");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.existsByName("Updated Fiction")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
            when(categoryMapper.toResponse(updatedCategory)).thenReturn(validCategoryResponse);

            // Act
            CategoryResponse response = categoryService.updateCategory(1L, updateRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when updating with duplicate name")
        void shouldThrowExceptionWhenDuplicateName() {
            // Arrange
            CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
            updateRequest.setName("Duplicate");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.existsByName("Duplicate")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.updateCategory(1L, updateRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category successfully")
        void shouldDeleteCategorySuccessfully() {
            // Arrange
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            doNothing().when(categoryRepository).delete(testCategory);

            // Act
            categoryService.deleteCategory(1L);

            // Assert
            verify(categoryRepository, times(1)).delete(testCategory);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent category")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            // Arrange
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(categoryRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw exception when deleting category with children")
        void shouldThrowExceptionWhenDeletingCategoryWithChildren() {
            // Arrange
            Category childCategory = TestFixtures.createCategoryWithParent(2L, "Sub-Fiction", "sub-fiction", testCategory);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(bookRepository.existsByCategoriesIdAndDeletedFalse(1L)).thenReturn(false);
            when(categoryRepository.existsByParentId(1L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Cannot delete category with subcategories");
        }
    }

    @Nested
    @DisplayName("Category Hierarchy Tests")
    class CategoryHierarchyTests {

        @Test
        @DisplayName("Should get root categories successfully")
        void shouldGetRootCategories() {
            // Arrange
            Page<Category> rootCategories = new PageImpl<>(List.of(testCategory), PageRequest.of(0, 20), 1);
            when(categoryRepository.findByParentIsNull(any(Pageable.class))).thenReturn(rootCategories);
            when(categoryMapper.toResponse(testCategory)).thenReturn(validCategoryResponse);

            // Act
            Page<CategoryResponse> response = categoryService.getRootCategories(PageRequest.of(0, 20));

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should get subcategories successfully")
        void shouldGetSubcategories() {
            // Arrange
            Category parentCategory = TestFixtures.createValidCategory();
            Page<Category> subcategories = new PageImpl<>(List.of(testCategory), PageRequest.of(0, 20), 1);

            when(categoryRepository.existsById(1L)).thenReturn(true);
            when(categoryRepository.findByParentId(eq(1L), any(Pageable.class))).thenReturn(subcategories);
            when(categoryMapper.toResponse(testCategory)).thenReturn(validCategoryResponse);

            // Act
            Page<CategoryResponse> response = categoryService.getSubcategories(1L, PageRequest.of(0, 20));

            // Assert
            assertThat(response).isNotNull();
            verify(categoryRepository, times(1)).findByParentId(eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("Should identify top-level categories")
        void shouldIdentifyTopLevelCategories() {
            // Arrange & Act
            boolean isTopLevel = testCategory.isTopLevel();

            // Assert
            assertThat(isTopLevel).isTrue();
        }

        @Test
        @DisplayName("Should validate hierarchy when moving category")
        void shouldValidateHierarchyWhenMoving() {
            // Arrange: childCategory has testCategory (id=1) as its parent
            // isValidHierarchy(categoryId=1, newParentId=2) calls findById(2) to get the new parent
            // then walks childCategory's parent chain looking for id=1 → finds it → cycle detected → invalid
            Category childCategory = TestFixtures.createCategoryWithParent(2L, "Sub-Fiction", "sub-fiction", testCategory);
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(childCategory));

            // Act
            boolean isValid = categoryService.isValidHierarchy(1L, 2L);

            // Assert
            assertThat(isValid).isFalse(); // Moving parent to child is invalid
        }
    }

    @Nested
    @DisplayName("Search Category Tests")
    class SearchCategoryTests {

        @Test
        @DisplayName("Should search categories by name")
        void shouldSearchCategoriesByName() {
            // Arrange
            Page<Category> searchResults = new PageImpl<>(List.of(testCategory), PageRequest.of(0, 20), 1);
            when(categoryRepository.findByNameContainingIgnoreCase("Fiction", PageRequest.of(0, 20)))
                    .thenReturn(searchResults);
            when(categoryMapper.toResponse(testCategory)).thenReturn(validCategoryResponse);

            // Act
            Page<CategoryResponse> response = categoryService.searchCategoriesByName("Fiction", PageRequest.of(0, 20));

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should get categories with books")
        void shouldGetCategoriesWithBooks() {
            // Arrange
            Page<Category> categoriesWithBooks = new PageImpl<>(List.of(testCategory), PageRequest.of(0, 20), 1);
            when(categoryRepository.findCategoriesWithBooks(any(Pageable.class))).thenReturn(categoriesWithBooks);
            when(categoryMapper.toResponse(testCategory)).thenReturn(validCategoryResponse);

            // Act
            Page<CategoryResponse> response = categoryService.getCategoriesWithBooks(PageRequest.of(0, 20));

            // Assert
            assertThat(response).isNotNull();
            verify(categoryRepository, times(1)).findCategoriesWithBooks(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get total category count")
        void shouldGetTotalCategoryCount() {
            // Arrange
            when(categoryRepository.count()).thenReturn(10L);

            // Act
            long count = categoryService.getTotalCategoryCount();

            // Assert
            assertThat(count).isEqualTo(10L);
            verify(categoryRepository, times(1)).count();
        }

        @Test
        @DisplayName("Should check if category exists by name")
        void shouldCheckIfCategoryExistsByName() {
            // Arrange
            when(categoryRepository.existsByName("Fiction")).thenReturn(true);

            // Act
            boolean exists = categoryService.existsByName("Fiction");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should check if category exists by slug")
        void shouldCheckIfCategoryExistsBySlug() {
            // Arrange
            when(categoryRepository.existsBySlug("fiction")).thenReturn(true);

            // Act
            boolean exists = categoryService.existsBySlug("fiction");

            // Assert
            assertThat(exists).isTrue();
        }
    }
}

