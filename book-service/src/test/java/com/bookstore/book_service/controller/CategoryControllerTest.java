package com.bookstore.book_service.controller;

import com.bookstore.book_service.TestFixtures;
import com.bookstore.book_service.dto.CategoryCreateRequest;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.dto.CategoryUpdateRequest;
import com.bookstore.book_service.exception.ResourceNotFoundException;
import com.bookstore.book_service.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc
@DisplayName("CategoryController Unit Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private CategoryCreateRequest validCategoryRequest;
    private CategoryResponse validCategoryResponse;

    @BeforeEach
    void setUp() {
        validCategoryRequest = TestFixtures.createValidCategoryRequest();
        validCategoryResponse = TestFixtures.createValidCategoryResponse();
    }

    @Nested
    @DisplayName("Get Categories Tests")
    class GetCategoriesTests {

        @Test
        @DisplayName("Should get all categories successfully")
        void shouldGetAllCategories() throws Exception {
            // Arrange
            Page<CategoryResponse> categoryPage = new PageImpl<>(List.of(validCategoryResponse), PageRequest.of(0, 20), 1);
            when(categoryService.getAllCategories(any())).thenReturn(categoryPage);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Fiction"));

            verify(categoryService, times(1)).getAllCategories(any());
        }

        @Test
        @DisplayName("Should get category by ID successfully")
        void shouldGetCategoryById() throws Exception {
            // Arrange
            when(categoryService.getCategoryById(1L)).thenReturn(validCategoryResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Fiction"));

            verify(categoryService, times(1)).getCategoryById(1L);
        }

        @Test
        @DisplayName("Should return 404 when category not found")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            // Arrange
            when(categoryService.getCategoryById(999L))
                    .thenThrow(new ResourceNotFoundException("Category not found"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should get category by slug successfully")
        void shouldGetCategoryBySlug() throws Exception {
            // Arrange
            when(categoryService.getCategoryBySlug("fiction")).thenReturn(validCategoryResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/slug/fiction")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.slug").value("fiction"));

            verify(categoryService, times(1)).getCategoryBySlug("fiction");
        }

        @Test
        @DisplayName("Should get root categories")
        void shouldGetRootCategories() throws Exception {
            // Arrange
            Page<CategoryResponse> rootCategories = new PageImpl<>(List.of(validCategoryResponse), PageRequest.of(0, 20), 1);
            when(categoryService.getRootCategories(any())).thenReturn(rootCategories);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/root")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(categoryService, times(1)).getRootCategories(any());
        }
    }

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() throws Exception {
            // Arrange
            when(categoryService.createCategory(any(CategoryCreateRequest.class)))
                    .thenReturn(validCategoryResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCategoryRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Fiction"));

            verify(categoryService, times(1)).createCategory(any(CategoryCreateRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request is invalid")
        void shouldReturn400WhenRequestInvalid() throws Exception {
            // Arrange
            CategoryCreateRequest invalidRequest = new CategoryCreateRequest();
            // Missing required fields

            // Act & Assert
            mockMvc.perform(post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(categoryService, never()).createCategory(any());
        }
    }

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category successfully")
        void shouldUpdateCategorySuccessfully() throws Exception {
            // Arrange
            CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
            updateRequest.setName("Updated Fiction");
            updateRequest.setDescription("Updated description");

            CategoryResponse updatedResponse = TestFixtures.createValidCategoryResponse();
            updatedResponse.setName("Updated Fiction");

            when(categoryService.updateCategory(eq(1L), any(CategoryUpdateRequest.class)))
                    .thenReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/v1/categories/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Fiction"));

            verify(categoryService, times(1)).updateCategory(eq(1L), any(CategoryUpdateRequest.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent category")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            // Arrange
            CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
            updateRequest.setName("Updated");

            when(categoryService.updateCategory(eq(999L), any(CategoryUpdateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Category not found"));

            // Act & Assert
            mockMvc.perform(put("/api/v1/categories/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category successfully")
        void shouldDeleteCategorySuccessfully() throws Exception {
            // Arrange
            doNothing().when(categoryService).deleteCategory(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/categories/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(categoryService, times(1)).deleteCategory(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent category")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Category not found"))
                    .when(categoryService).deleteCategory(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/categories/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Hierarchy Tests")
    class HierarchyTests {

        @Test
        @DisplayName("Should get category hierarchy")
        void shouldGetCategoryHierarchy() throws Exception {
            // Arrange
            List<CategoryResponse> hierarchy = List.of(validCategoryResponse);
            when(categoryService.getCategoryHierarchy()).thenReturn(hierarchy);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/hierarchy")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(categoryService, times(1)).getCategoryHierarchy();
        }

        @Test
        @DisplayName("Should get subcategories")
        void shouldGetSubcategories() throws Exception {
            // Arrange
            Page<CategoryResponse> subcategories = new PageImpl<>(List.of(validCategoryResponse), PageRequest.of(0, 20), 1);
            when(categoryService.getSubcategories(eq(1L), any())).thenReturn(subcategories);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/1/subcategories")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(categoryService, times(1)).getSubcategories(eq(1L), any());
        }

        @Test
        @DisplayName("Should move category to new parent")
        void shouldMoveCategoryToNewParent() throws Exception {
            // Arrange
            when(categoryService.moveCategory(eq(1L), eq(2L)))
                    .thenReturn(validCategoryResponse);

            // Act & Assert
            mockMvc.perform(put("/api/v1/categories/1/move")
                    .param("newParentId", "2")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(categoryService, times(1)).moveCategory(eq(1L), eq(2L));
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should search categories by name")
        void shouldSearchCategoriesByName() throws Exception {
            // Arrange
            Page<CategoryResponse> searchResults = new PageImpl<>(List.of(validCategoryResponse), PageRequest.of(0, 20), 1);
            when(categoryService.searchCategoriesByName(eq("Fiction"), any())).thenReturn(searchResults);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/search")
                    .param("name", "Fiction")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(categoryService, times(1)).searchCategoriesByName(eq("Fiction"), any());
        }

        @Test
        @DisplayName("Should get categories with books")
        void shouldGetCategoriesWithBooks() throws Exception {
            // Arrange
            Page<CategoryResponse> categoriesWithBooks = new PageImpl<>(List.of(validCategoryResponse), PageRequest.of(0, 20), 1);
            when(categoryService.getCategoriesWithBooks(any())).thenReturn(categoriesWithBooks);

            // Act & Assert
            mockMvc.perform(get("/api/v1/categories/with-books")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(categoryService, times(1)).getCategoriesWithBooks(any());
        }
    }
}

