package com.bookstore.book_service;

import com.bookstore.book_service.dto.CategoryCreateRequest;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.dto.CategoryUpdateRequest;
import com.bookstore.book_service.model.Category;
import com.bookstore.book_service.repository.CategoryRepository;
import com.bookstore.book_service.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Category Service Integration Tests")
@Transactional
class CategoryServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    private CategoryCreateRequest validCategoryRequest;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        validCategoryRequest = TestFixtures.createValidCategoryRequest();
    }

    @Nested
    @DisplayName("Category Creation Integration Tests")
    class CategoryCreationTests {

        @Test
        @DisplayName("Should create and persist category successfully via HTTP")
        void shouldCreateAndPersistCategory() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCategoryRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("Fiction"));

            // Verify in database
            assertThat(categoryRepository.existsByName("Fiction")).isTrue();
        }

        @Test
        @DisplayName("Should prevent duplicate category name")
        void shouldPreventDuplicateCategoryName() throws Exception {
            // Arrange - Create first category
            mockMvc.perform(post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCategoryRequest)))
                    .andExpect(status().isCreated());

            // Act - Try to create second category with same name
            mockMvc.perform(post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCategoryRequest)))
                    .andExpect(status().isConflict());

            // Verify only one category exists
            assertThat(categoryRepository.existsByName("Fiction")).isTrue();
            assertThat(categoryRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should allow creating categories with different names (slugs auto-generated)")
        void shouldPreventDuplicateCategorySlug() throws Exception {
            // Arrange
            CategoryCreateRequest firstRequest = validCategoryRequest; // name="Fiction" -> slug="fiction"

            // A category with a different name will get a different slug
            CategoryCreateRequest secondRequest = new CategoryCreateRequest();
            secondRequest.setName("Thriller");
            secondRequest.setDescription("Thriller books");

            // Act - Create first category
            mockMvc.perform(post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isCreated());

            // Thriller has a different name/slug so it should succeed
            mockMvc.perform(post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isCreated());

            // Both categories exist
            assertThat(categoryRepository.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Category Retrieval Integration Tests")
    class CategoryRetrievalTests {

        @Test
        @DisplayName("Should retrieve category by ID")
        void shouldRetrieveCategoryById() throws Exception {
            // Arrange
            Category category = TestFixtures.createValidCategory();
            category.setId(null);
            Category savedCategory = categoryRepository.save(category);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/" + savedCategory.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedCategory.getId()))
                    .andExpect(jsonPath("$.name").value("Fiction"));
        }

        @Test
        @DisplayName("Should retrieve category by slug")
        void shouldRetrieveCategoryBySlug() throws Exception {
            // Arrange
            Category category = TestFixtures.createValidCategory();
            category.setId(null);
            categoryRepository.save(category);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/slug/fiction")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.slug").value("fiction"));
        }

        @Test
        @DisplayName("Should retrieve all categories with pagination")
        void shouldRetrieveAllCategoriesWithPagination() throws Exception {
            // Arrange - Create multiple categories
            for (int i = 0; i < 5; i++) {
                Category category = TestFixtures.createCategory((long)i, "Category " + i, "cat-" + i);
                category.setId(null);
                categoryRepository.save(category);
            }

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories")
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements").value(5));
        }
    }

    @Nested
    @DisplayName("Category Update Integration Tests")
    class CategoryUpdateTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() throws Exception {
            // Arrange
            Category category = TestFixtures.createValidCategory();
            category.setId(null);
            Category savedCategory = categoryRepository.save(category);

            CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
            updateRequest.setName("Updated Fiction");
            updateRequest.setDescription("Updated description");

            // Act
            mockMvc.perform(put("/api/v1/categories/" + savedCategory.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Fiction"));

            // Assert - Verify in database
            Optional<Category> updatedCategory = categoryRepository.findById(savedCategory.getId());
            assertThat(updatedCategory).isPresent();
            assertThat(updatedCategory.get().getName()).isEqualTo("Updated Fiction");
        }
    }

    @Nested
    @DisplayName("Category Deletion Integration Tests")
    class CategoryDeletionTests {

        @Test
        @DisplayName("Should delete category successfully")
        void shouldDeleteCategorySuccessfully() throws Exception {
            // Arrange
            Category category = TestFixtures.createValidCategory();
            category.setId(null);
            Category savedCategory = categoryRepository.save(category);

            // Act
            mockMvc.perform(delete("/api/v1/categories/" + savedCategory.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // Assert
            Optional<Category> deletedCategory = categoryRepository.findById(savedCategory.getId());
            assertThat(deletedCategory).isEmpty();
        }

        @Test
        @DisplayName("Should not delete category with children")
        void shouldNotDeleteCategoryWithChildren() throws Exception {
            // Arrange
            Category parentCategory = TestFixtures.createValidCategory();
            parentCategory.setId(null);
            Category savedParent = categoryRepository.save(parentCategory);

            Category childCategory = TestFixtures.createCategoryWithParent(null, "Sub-Category", "sub-cat", savedParent);
            categoryRepository.save(childCategory);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/categories/" + savedParent.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            // Verify parent still exists
            assertThat(categoryRepository.findById(savedParent.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("Category Hierarchy Integration Tests")
    class CategoryHierarchyTests {

        @Test
        @DisplayName("Should create category hierarchy successfully")
        void shouldCreateCategoryHierarchy() throws Exception {
            // Arrange
            Category parentCategory = TestFixtures.createValidCategory();
            parentCategory.setId(null);
            Category savedParent = categoryRepository.save(parentCategory);

            // Act - Get hierarchy
            mockMvc.perform(get("/api/v1/categories/hierarchy")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should get root categories only")
        void shouldGetRootCategoriesOnly() throws Exception {
            // Arrange - Create parent and child categories
            Category parent = TestFixtures.createValidCategory();
            parent.setId(null);
            Category savedParent = categoryRepository.save(parent);

            Category child = TestFixtures.createCategoryWithParent(null, "Sub-Category", "sub-cat", savedParent);
            categoryRepository.save(child);

            // Act & Assert - Get root categories
            mockMvc.perform(get("/api/v1/categories/root")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.page.totalElements").value(1));
        }

        @Test
        @DisplayName("Should get subcategories of parent")
        void shouldGetSubcategoriesOfParent() throws Exception {
            // Arrange
            Category parent = TestFixtures.createValidCategory();
            parent.setId(null);
            Category savedParent = categoryRepository.save(parent);

            Category child1 = TestFixtures.createCategoryWithParent(null, "Sub1", "sub1", savedParent);
            Category child2 = TestFixtures.createCategoryWithParent(null, "Sub2", "sub2", savedParent);
            categoryRepository.save(child1);
            categoryRepository.save(child2);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/" + savedParent.getId() + "/subcategories")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should move category to new parent")
        void shouldMoveCategoryToNewParent() throws Exception {
            // Arrange
            Category parent1 = TestFixtures.createValidCategory();
            parent1.setId(null);
            Category savedParent1 = categoryRepository.save(parent1);

            Category parent2 = TestFixtures.createCategory(null, "Parent2", "parent2");
            parent2.setId(null);
            Category savedParent2 = categoryRepository.save(parent2);

            Category child = TestFixtures.createCategoryWithParent(null, "Child", "child", savedParent1);
            Category savedChild = categoryRepository.save(child);

            // Act
            mockMvc.perform(put("/api/v1/categories/" + savedChild.getId() + "/move")
                            .param("newParentId", savedParent2.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Assert
            Optional<Category> movedCategory = categoryRepository.findById(savedChild.getId());
            assertThat(movedCategory).isPresent();
            assertThat(movedCategory.get().getParent().getId()).isEqualTo(savedParent2.getId());
        }
    }

    @Nested
    @DisplayName("Category Search Integration Tests")
    class CategorySearchTests {

        @Test
        @DisplayName("Should search categories by name")
        void shouldSearchCategoriesByName() throws Exception {
            // Arrange
            Category category1 = TestFixtures.createValidCategory();
            category1.setId(null);
            categoryRepository.save(category1);

            Category category2 = TestFixtures.createCategory(null, "Non-Fiction", "non-fiction");
            category2.setId(null);
            categoryRepository.save(category2);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/search")
                    .param("name", "Fiction")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("Category Statistics Integration Tests")
    class CategoryStatisticsTests {

        @Test
        @DisplayName("Should get total category count")
        void shouldGetTotalCategoryCount() throws Exception {
            // Arrange - Create multiple categories
            for (int i = 0; i < 3; i++) {
                Category category = TestFixtures.createCategory((long)i, "Category " + i, "cat-" + i);
                category.setId(null);
                categoryRepository.save(category);
            }

            // Act
            long count = categoryService.getTotalCategoryCount();

            // Assert
            assertThat(count).isEqualTo(3);
        }
    }
}

