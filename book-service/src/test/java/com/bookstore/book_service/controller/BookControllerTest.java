package com.bookstore.book_service.controller;

import com.bookstore.book_service.TestFixtures;
import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.BookUpdateRequest;
import com.bookstore.book_service.exception.ResourceNotFoundException;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.service.BookService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc
@DisplayName("BookController Unit Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private BookCreateRequest validBookRequest;
    private BookResponse validBookResponse;

    @BeforeEach
    void setUp() {
        validBookRequest = TestFixtures.createValidBookRequest();
        validBookResponse = TestFixtures.createValidBookResponse();
    }

    @Nested
    @DisplayName("Get Books Tests")
    class GetBooksTests {

        @Test
        @DisplayName("Should get all books successfully")
        void shouldGetAllBooks() throws Exception {
            // Arrange
            Page<BookResponse> bookPage = new PageImpl<>(List.of(validBookResponse), PageRequest.of(0, 20), 1);
            when(bookService.findAllBooks(any())).thenReturn(bookPage);

            // Act & Assert
            mockMvc.perform(get("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("The Great Gatsby"));

            verify(bookService, times(1)).findAllBooks(any());
        }

        @Test
        @DisplayName("Should get book by ID successfully")
        void shouldGetBookById() throws Exception {
            // Arrange
            when(bookService.getBookById(1L)).thenReturn(validBookResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("The Great Gatsby"))
                    .andExpect(jsonPath("$.author").value("F. Scott Fitzgerald"));

            verify(bookService, times(1)).getBookById(1L);
        }

        @Test
        @DisplayName("Should return 404 when book not found")
        void shouldReturn404WhenBookNotFound() throws Exception {
            // Arrange
            when(bookService.getBookById(999L)).thenThrow(new ResourceNotFoundException("Book not found"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(bookService, times(1)).getBookById(999L);
        }

        @Test
        @DisplayName("Should get books by status")
        void shouldGetBooksByStatus() throws Exception {
            // Arrange
            Page<BookResponse> bookPage = new PageImpl<>(List.of(validBookResponse), PageRequest.of(0, 20), 1);
            when(bookService.getBooksByStatus(eq(BookStatus.AVAILABLE), any())).thenReturn(bookPage);

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/status/AVAILABLE")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"));

            verify(bookService, times(1)).getBooksByStatus(eq(BookStatus.AVAILABLE), any());
        }
    }

    @Nested
    @DisplayName("Create Book Tests")
    class CreateBookTests {

        @Test
        @DisplayName("Should create book successfully")
        void shouldCreateBookSuccessfully() throws Exception {
            // Arrange
            when(bookService.createBook(any(BookCreateRequest.class))).thenReturn(validBookResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validBookRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("The Great Gatsby"));

            verify(bookService, times(1)).createBook(any(BookCreateRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request is invalid")
        void shouldReturn400WhenRequestInvalid() throws Exception {
            // Arrange
            BookCreateRequest invalidRequest = new BookCreateRequest();
            invalidRequest.setTitle(""); // Invalid: blank title

            // Act & Assert
            mockMvc.perform(post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any());
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            // Arrange
            BookCreateRequest incompleteRequest = new BookCreateRequest();
            incompleteRequest.setTitle("Title");
            // Missing author, price, totalCopies

            // Act & Assert
            mockMvc.perform(post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(incompleteRequest)))
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any());
        }
    }

    @Nested
    @DisplayName("Update Book Tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Should update book successfully")
        void shouldUpdateBookSuccessfully() throws Exception {
            // Arrange
            BookUpdateRequest updateRequest = TestFixtures.createValidBookUpdateRequest();
            BookResponse updatedResponse = TestFixtures.createValidBookResponse();
            updatedResponse.setTitle("Updated Title");

            when(bookService.updateBook(eq(1L), any(BookUpdateRequest.class)))
                    .thenReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/v1/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"));

            verify(bookService, times(1)).updateBook(eq(1L), any(BookUpdateRequest.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent book")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            // Arrange
            BookUpdateRequest updateRequest = TestFixtures.createValidBookUpdateRequest();
            when(bookService.updateBook(eq(999L), any(BookUpdateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Book not found"));

            // Act & Assert
            mockMvc.perform(put("/api/v1/books/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Delete Book Tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should delete book successfully")
        void shouldDeleteBookSuccessfully() throws Exception {
            // Arrange
            doNothing().when(bookService).deleteBookById(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/books/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(bookService, times(1)).deleteBookById(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent book")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Book not found"))
                    .when(bookService).deleteBookById(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/books/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should search books by keyword")
        void shouldSearchBooksByKeyword() throws Exception {
            // Arrange
            Page<BookResponse> searchResults = new PageImpl<>(List.of(validBookResponse), PageRequest.of(0, 20), 1);
            when(bookService.searchBooks(eq("Gatsby"), any())).thenReturn(searchResults);

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/search")
                    .param("keyword", "Gatsby")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title").value("The Great Gatsby"));

            verify(bookService, times(1)).searchBooks(eq("Gatsby"), any());
        }

        @Test
        @DisplayName("Should perform advanced search")
        void shouldPerformAdvancedSearch() throws Exception {
            // Arrange
            Page<BookResponse> searchResults = new PageImpl<>(List.of(validBookResponse), PageRequest.of(0, 20), 1);
            when(bookService.searchBooksAdvanced(
                    any(), any(), any(), any(), any(),
                    any(), any(), any(), any(),
                    any(), any()
            )).thenReturn(searchResults);

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/search/advanced")
                    .param("title", "Gatsby")
                    .param("author", "Fitzgerald")
                    .param("status", "AVAILABLE")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());


            verify(bookService, times(1)).searchBooksAdvanced(
                    any(), any(), any(), any(), any(),
                    any(), any(), any(), any(),
                    any(), any()
            );
        }
    }

    @Nested
    @DisplayName("Latest Books Tests")
    class LatestBooksTests {

        @Test
        @DisplayName("Should get latest books")
        void shouldGetLatestBooks() throws Exception {
            // Arrange
            List<BookResponse> latestBooks = List.of(validBookResponse);
            when(bookService.getLatestBooks(10)).thenReturn(latestBooks);

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/latest")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].title").value("The Great Gatsby"));

            verify(bookService, times(1)).getLatestBooks(10);
        }

        @Test
        @DisplayName("Should get latest books with default limit")
        void shouldGetLatestBooksWithDefaultLimit() throws Exception {
            // Arrange
            List<BookResponse> latestBooks = List.of(validBookResponse);
            when(bookService.getLatestBooks(10)).thenReturn(latestBooks);

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/latest")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(bookService, times(1)).getLatestBooks(10);
        }
    }

    @Nested
    @DisplayName("Category Management Tests")
    class CategoryManagementTests {

        @Test
        @DisplayName("Should add categories to book successfully")
        void shouldAddCategoriesToBook() throws Exception {
            // Arrange
            Set<Long> categoryIds = TestFixtures.createCategoryIds(1L, 2L);
            when(bookService.addCategoriesToBook(eq(1L), eq(categoryIds)))
                    .thenReturn(validBookResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/books/1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryIds)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(bookService, times(1)).addCategoriesToBook(eq(1L), any());
        }

        @Test
        @DisplayName("Should return 404 when book not found when adding categories")
        void shouldReturn404WhenBookNotFoundForCategories() throws Exception {
            // Arrange
            Set<Long> categoryIds = TestFixtures.createCategoryIds(1L);
            when(bookService.addCategoriesToBook(eq(999L), eq(categoryIds)))
                    .thenThrow(new ResourceNotFoundException("Book not found"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/books/999/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryIds)))
                    .andExpect(status().isNotFound());
        }
    }
}

