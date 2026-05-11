package com.bookstore.book_service;

import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.BookUpdateRequest;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.model.Category;
import com.bookstore.book_service.repository.BookRepository;
import com.bookstore.book_service.repository.CategoryRepository;
import com.bookstore.book_service.service.BookService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Book Service Integration Tests")
@Transactional
class BookServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookService bookService;

    private BookCreateRequest validBookRequest;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        categoryRepository.deleteAll();

        validBookRequest = TestFixtures.createValidBookRequest();

        // Create test category
        testCategory = Category.builder()
                .name("Integration Test Category")
                .slug("int-test-cat")
                .description("Category for integration tests")
                .children(new HashSet<>())
                .books(new HashSet<>())
                .build();
        categoryRepository.save(testCategory);
    }

    @Nested
    @DisplayName("Book Creation Integration Tests")
    class BookCreationTests {

        @Test
        @DisplayName("Should create and persist book successfully via HTTP")
        void shouldCreateAndPersistBook() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validBookRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.title").value("The Great Gatsby"));

            // Verify in database
            Optional<Book> savedBook = bookRepository.findByIsbnAndDeletedFalse("9780743273565");
            assertThat(savedBook).isPresent();
            assertThat(savedBook.get().getTitle()).isEqualTo("The Great Gatsby");
            assertThat(savedBook.get().getAuthor()).isEqualTo("F. Scott Fitzgerald");
        }

        @Test
        @DisplayName("Should create book with categories")
        void shouldCreateBookWithCategories() throws Exception {
            // Arrange
            validBookRequest.setCategoryIds(java.util.Set.of(testCategory.getId()));

            // Act
            mockMvc.perform(post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validBookRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.categories").isArray());

            // Verify in database
            Optional<Book> savedBook = bookRepository.findByIsbnAndDeletedFalse("9780743273565");
            assertThat(savedBook).isPresent();
            assertThat(savedBook.get().getCategories()).hasSize(1);
        }

        @Test
        @DisplayName("Should prevent duplicate ISBN creation")
        void shouldPreventDuplicateIsbn() throws Exception {
            // Arrange - Create first book
            mockMvc.perform(post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validBookRequest)))
                    .andExpect(status().isCreated());

            // Act - Try to create second book with same ISBN
            mockMvc.perform(post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validBookRequest)))
                    .andExpect(status().isConflict());

            // Verify only one book exists
            assertThat(bookRepository.findByIsbnAndDeletedFalse("9780743273565")).isPresent();
        }
    }

    @Nested
    @DisplayName("Book Retrieval Integration Tests")
    class BookRetrievalTests {

        @Test
        @DisplayName("Should retrieve book by ID")
        void shouldRetrieveBookById() throws Exception {
            // Arrange - Create a book
            Book book = TestFixtures.createValidBook();
            book.setId(null);
            Book savedBook = bookRepository.save(book);

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/" + savedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedBook.getId()))
                    .andExpect(jsonPath("$.title").value("The Great Gatsby"));
        }

        @Test
        @DisplayName("Should retrieve all books with pagination")
        void shouldRetrieveAllBooksWithPagination() throws Exception {
            // Arrange - Create multiple books
            for (int i = 0; i < 5; i++) {
                Book book = TestFixtures.createBook((long)i, "Book " + i, "Author " + i,
                        "978074327356" + i, 100, 100);
                book.setId(null);
                bookRepository.save(book);
            }

            // Act & Assert
            mockMvc.perform(get("/api/v1/books")
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.page.totalElements").value(5));
        }

        @Test
        @DisplayName("Should filter books by status")
        void shouldFilterBooksByStatus() throws Exception {
            // Arrange
            Book availableBook = TestFixtures.createValidBook();
            availableBook.setId(null);
            availableBook.setStatus(BookStatus.AVAILABLE);
            bookRepository.save(availableBook);

            Book unavailableBook = TestFixtures.createValidBook();
            unavailableBook.setId(null);
            unavailableBook.setIsbn("9999999999999");
            unavailableBook.setStatus(BookStatus.DISCONTINUED);
            bookRepository.save(unavailableBook);

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/status/AVAILABLE")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("Book Update Integration Tests")
    class BookUpdateTests {

        @Test
        @DisplayName("Should update book successfully")
        void shouldUpdateBookSuccessfully() throws Exception {
            // Arrange - Create a book
            Book book = TestFixtures.createValidBook();
            book.setId(null);
            Book savedBook = bookRepository.save(book);

            BookUpdateRequest updateRequest = new BookUpdateRequest();
            updateRequest.setTitle("Updated Title");
            updateRequest.setPrice(new BigDecimal("25.99"));

            // Act
            mockMvc.perform(put("/api/v1/books/" + savedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"));

            // Assert - Verify in database
            Optional<Book> updatedBook = bookRepository.findByIdAndDeletedFalse(savedBook.getId());
            assertThat(updatedBook).isPresent();
            assertThat(updatedBook.get().getTitle()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("Should update book inventory successfully")
        void shouldUpdateBookInventorySuccessfully() throws Exception {
            // Arrange
            Book book = TestFixtures.createValidBook();
            book.setId(null);
            book.setTotalCopies(100);
            book.setAvailableCopies(50);
            Book savedBook = bookRepository.save(book);

            // Act - Make API call directly to service
            BookResponse response = bookService.updateInventory(savedBook.getId(), 200, 150);

            // Assert
            assertThat(response.getTotalCopies()).isEqualTo(200);
            assertThat(response.getAvailableCopies()).isEqualTo(150);

            Optional<Book> updatedBook = bookRepository.findByIdAndDeletedFalse(savedBook.getId());
            assertThat(updatedBook).isPresent();
            assertThat(updatedBook.get().getTotalCopies()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Book Deletion Integration Tests")
    class BookDeletionTests {

        @Test
        @DisplayName("Should soft delete book successfully")
        void shouldSoftDeleteBook() throws Exception {
            // Arrange
            Book book = TestFixtures.createValidBook();
            book.setId(null);
            Book savedBook = bookRepository.save(book);

            // Act
            mockMvc.perform(delete("/api/v1/books/" + savedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // Assert - Book marked as deleted but not removed from database
            Optional<Book> deletedBook = bookRepository.findById(savedBook.getId());
            assertThat(deletedBook).isPresent();
            assertThat(deletedBook.get().getDeleted()).isTrue();

            // Book should not appear in active book queries
            Optional<Book> activeBook = bookRepository.findByIdAndDeletedFalse(savedBook.getId());
            assertThat(activeBook).isEmpty();
        }

        @Test
        @DisplayName("Should not retrieve deleted books")
        void shouldNotRetrieveDeletedBooks() throws Exception {
            // Arrange
            Book book = TestFixtures.createValidBook();
            book.setId(null);
            Book savedBook = bookRepository.save(book);

            // Act - Delete the book
            mockMvc.perform(delete("/api/v1/books/" + savedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // Assert - Book should not be retrievable
            mockMvc.perform(get("/api/v1/books/" + savedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Book Search Integration Tests")
    class BookSearchTests {

        @Test
        @DisplayName("Should search books by keyword")
        void shouldSearchBooksByKeyword() throws Exception {
            // Arrange - Create test books
            createTestBooks();

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/search")
                    .param("keyword", "Gatsby")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title").value("The Great Gatsby"));
        }

        @Test
        @DisplayName("Should perform advanced search with multiple filters")
        void shouldPerformAdvancedSearch() throws Exception {
            // Arrange
            createTestBooks();

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/search/advanced")
                    .param("title", "Great")
                    .param("author", "Fitzgerald")
                    .param("status", "AVAILABLE")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should get latest books")
        void shouldGetLatestBooks() throws Exception {
            // Arrange
            createTestBooks();

            // Act & Assert
            mockMvc.perform(get("/api/v1/books/latest")
                    .param("limit", "5")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Book Inventory Management Integration Tests")
    class InventoryManagementTests {

        @Test
        @DisplayName("Should reserve copies successfully")
        void shouldReserveCopiesSuccessfully() throws Exception {
            // Arrange
            Book book = TestFixtures.createValidBook();
            book.setId(null);
            book.setAvailableCopies(100);
            Book savedBook = bookRepository.save(book);

            // Act
            BookResponse response = bookService.reserveCopies(savedBook.getId(), 10);

            // Assert
            assertThat(response.getAvailableCopies()).isEqualTo(90);
        }

        @Test
        @DisplayName("Should fail to reserve more copies than available")
        void shouldFailToReserveMoreThanAvailable() throws Exception {
            // Arrange
            Book book = TestFixtures.createValidBook();
            book.setId(null);
            book.setAvailableCopies(5);
            Book savedBook = bookRepository.save(book);

            // Act & Assert
            assertThatThrownBy(() -> bookService.reserveCopies(savedBook.getId(), 10))
                    .isInstanceOf(com.bookstore.book_service.exception.InsufficientInventoryException.class)
                    .hasMessageContaining("Not enough copies available");
        }

        @Test
        @DisplayName("Should release copies successfully")
        void shouldReleaseCopiesSuccessfully() throws Exception {
            // Arrange
            Book book = TestFixtures.createValidBook();
            book.setId(null);
            book.setAvailableCopies(95);
            book.setTotalCopies(100);
            Book savedBook = bookRepository.save(book);

            // Act
            BookResponse response = bookService.releaseCopies(savedBook.getId(), 5);

            // Assert
            assertThat(response.getAvailableCopies()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Category Management Integration Tests")
    class CategoryManagementTests {

        @Test
        @DisplayName("Should add categories to book successfully")
        void shouldAddCategoriesToBook() throws Exception {
            // Arrange
            Book book = TestFixtures.createValidBook();
            book.setId(null);
            Book savedBook = bookRepository.save(book);

            java.util.Set<Long> categoryIds = java.util.Set.of(testCategory.getId());

            // Act
            mockMvc.perform(post("/api/v1/books/" + savedBook.getId() + "/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryIds)))
                    .andExpect(status().isOk());

            // Assert
            Optional<Book> bookWithCategories = bookRepository.findByIdAndDeletedFalse(savedBook.getId());
            assertThat(bookWithCategories).isPresent();
            assertThat(bookWithCategories.get().getCategories()).hasSize(1);
        }
    }

    // ================== Helper Methods ==================

    private void createTestBooks() {
        Book book1 = TestFixtures.createValidBook();
        book1.setId(null);
        bookRepository.save(book1);

        Book book2 = TestFixtures.createBook(null, "To Kill a Mockingbird", "Harper Lee",
                "9780061120084", 50, 50);
        bookRepository.save(book2);

        Book book3 = TestFixtures.createBook(null, "1984", "George Orwell",
                "9780451524935", 75, 75);
        bookRepository.save(book3);
    }
}

