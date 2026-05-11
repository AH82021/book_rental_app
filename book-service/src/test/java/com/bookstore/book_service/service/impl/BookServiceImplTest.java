package com.bookstore.book_service.service.impl;

import com.bookstore.book_service.TestFixtures;
import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.BookUpdateRequest;
import com.bookstore.book_service.exception.DuplicateResourceException;
import com.bookstore.book_service.exception.InsufficientInventoryException;
import com.bookstore.book_service.exception.InvalidRequestException;
import com.bookstore.book_service.exception.ResourceNotFoundException;
import com.bookstore.book_service.mapper.BookMapper;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookServiceImpl Unit Tests")
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private BookCreateRequest validBookRequest;
    private BookResponse validBookResponse;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testBook = TestFixtures.createValidBook();
        validBookRequest = TestFixtures.createValidBookRequest();
        validBookResponse = TestFixtures.createValidBookResponse();
        testCategory = TestFixtures.createValidCategory();
    }

    @Nested
    @DisplayName("Create Book Tests")
    class CreateBookTests {

        @Test
        @DisplayName("Should create book successfully with valid request")
        void shouldCreateBookSuccessfully() {
            // Arrange
            when(bookRepository.existsByIsbnAndDeletedFalse(anyString())).thenReturn(false);
            when(bookMapper.toEntity(validBookRequest)).thenReturn(testBook);
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);
            when(bookMapper.toResponse(testBook)).thenReturn(validBookResponse);

            // Act
            BookResponse response = bookService.createBook(validBookRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("The Great Gatsby");
            verify(bookRepository, times(1)).save(any(Book.class));
            verify(bookMapper, times(1)).toEntity(validBookRequest);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when ISBN already exists")
        void shouldThrowExceptionWhenIsbnDuplicate() {
            // Arrange
            when(bookRepository.existsByIsbnAndDeletedFalse(validBookRequest.getIsbn()))
                    .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> bookService.createBook(validBookRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already exists");

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidRequestException when available copies exceed total copies")
        void shouldThrowExceptionWhenAvailableExceedsTotal() {
            // Arrange
            BookCreateRequest invalidRequest = TestFixtures.createBookRequest(
                    "Title", "Author", "1234567890", "Description",
                    new BigDecimal("10.00"), new BigDecimal("5.00"), 50, 100
            );
            when(bookRepository.existsByIsbnAndDeletedFalse(anyString())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> bookService.createBook(invalidRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Available copies cannot exceed total copies");

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create book with categories successfully")
        void shouldCreateBookWithCategories() {
            // Arrange
            Set<Long> categoryIds = TestFixtures.createCategoryIds(1L);
            validBookRequest.setCategoryIds(categoryIds);
            Set<Category> categories = Set.of(testCategory);

            when(bookRepository.existsByIsbnAndDeletedFalse(anyString())).thenReturn(false);
            when(categoryRepository.findAllById(categoryIds)).thenReturn(List.of(testCategory));
            when(bookMapper.toEntity(validBookRequest)).thenReturn(testBook);
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);
            when(bookMapper.toResponse(testBook)).thenReturn(validBookResponse);

            // Act
            BookResponse response = bookService.createBook(validBookRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(categoryRepository, times(1)).findAllById(categoryIds);
        }
    }

    @Nested
    @DisplayName("Get Book Tests")
    class GetBookTests {

        @Test
        @DisplayName("Should get book by ID successfully")
        void shouldGetBookById() {
            // Arrange
            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));
            when(bookMapper.toResponse(testBook)).thenReturn(validBookResponse);

            // Act
            BookResponse response = bookService.getBookById(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            verify(bookRepository, times(1)).findByIdAndDeletedFalse(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when book not found")
        void shouldThrowExceptionWhenBookNotFound() {
            // Arrange
            when(bookRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.getBookById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Book not found");
        }

        @Test
        @DisplayName("Should get book by ISBN successfully")
        void shouldGetBookByIsbn() {
            // Arrange
            when(bookRepository.findByIsbnAndDeletedFalse("9780743273565")).thenReturn(Optional.of(testBook));
            when(bookMapper.toResponse(testBook)).thenReturn(validBookResponse);

            // Act
            BookResponse response = bookService.getBookByIsbn("9780743273565");

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getIsbn()).isEqualTo("9780743273565");
        }

        @Test
        @DisplayName("Should get all books with pagination")
        void shouldGetAllBooks() {
            // Arrange
            Page<Book> bookPage = new PageImpl<>(List.of(testBook), PageRequest.of(0, 20), 1);
            when(bookRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(bookPage);
            when(bookMapper.toResponse(testBook)).thenReturn(validBookResponse);

            // Act
            Page<BookResponse> response = bookService.findAllBooks(PageRequest.of(0, 20));

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1);
            verify(bookRepository, times(1)).findByDeletedFalse(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Update Book Tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Should update book successfully")
        void shouldUpdateBookSuccessfully() {
            // Arrange
            BookUpdateRequest updateRequest = TestFixtures.createValidBookUpdateRequest();
            Book updatedBook = TestFixtures.createValidBook();
            updatedBook.setTitle("Updated Title");

            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
            when(bookMapper.toResponse(updatedBook)).thenReturn(validBookResponse);
            doNothing().when(bookMapper).updateEntity(any(Book.class), any(BookUpdateRequest.class));

            // Act
            BookResponse response = bookService.updateBook(1L, updateRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw exception when updating with invalid inventory")
        void shouldThrowExceptionWhenInvalidInventory() {
            // Arrange
            BookUpdateRequest invalidRequest = new BookUpdateRequest();
            invalidRequest.setTotalCopies(50);
            invalidRequest.setAvailableCopies(100); // Available > Total

            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));

            // Act & Assert
            assertThatThrownBy(() -> bookService.updateBook(1L, invalidRequest))
                    .isInstanceOf(InvalidRequestException.class);
            verify(bookRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Book Tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should soft delete book successfully")
        void shouldDeleteBookSuccessfully() {
            // Arrange
            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);

            // Act
            bookService.deleteBookById(1L);

            // Assert
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent book")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            // Arrange
            when(bookRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.deleteBookById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Inventory Management Tests")
    class InventoryManagementTests {

        @Test
        @DisplayName("Should reserve copies successfully")
        void shouldReserveCopiesSuccessfully() {
            // Arrange
            testBook.setAvailableCopies(100);
            Book updatedBook = TestFixtures.createValidBook();
            updatedBook.setAvailableCopies(95);

            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
            when(bookMapper.toResponse(updatedBook)).thenReturn(validBookResponse);

            // Act
            BookResponse response = bookService.reserveCopies(1L, 5);

            // Assert
            assertThat(response).isNotNull();
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw exception when reserving more copies than available")
        void shouldThrowExceptionWhenInsufficientCopies() {
            // Arrange
            testBook.setAvailableCopies(5);
            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));

            // Act & Assert
            assertThatThrownBy(() -> bookService.reserveCopies(1L, 10))
                    .isInstanceOf(InsufficientInventoryException.class);
            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should release copies successfully")
        void shouldReleaseCopiesSuccessfully() {
            // Arrange
            testBook.setAvailableCopies(95);
            testBook.setTotalCopies(100);
            Book updatedBook = TestFixtures.createValidBook();
            updatedBook.setAvailableCopies(100);

            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
            when(bookMapper.toResponse(updatedBook)).thenReturn(validBookResponse);

            // Act
            BookResponse response = bookService.releaseCopies(1L, 5);

            // Assert
            assertThat(response).isNotNull();
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should update inventory successfully")
        void shouldUpdateInventorySuccessfully() {
            // Arrange
            Book updatedBook = TestFixtures.createValidBook();
            updatedBook.setTotalCopies(200);
            updatedBook.setAvailableCopies(200);

            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
            when(bookMapper.toResponse(updatedBook)).thenReturn(validBookResponse);

            // Act
            BookResponse response = bookService.updateInventory(1L, 200, 200);

            // Assert
            assertThat(response).isNotNull();
            verify(bookRepository, times(1)).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should search books by keyword")
        void shouldSearchBooksByKeyword() {
            // Arrange
            Page<Book> searchResults = new PageImpl<>(List.of(testBook), PageRequest.of(0, 20), 1);
            when(bookRepository.searchByKeyword(eq("Gatsby"), any(Pageable.class)))
                    .thenReturn(searchResults);
            when(bookMapper.toResponse(testBook)).thenReturn(validBookResponse);

            // Act
            Page<BookResponse> response = bookService.searchBooks("Gatsby", PageRequest.of(0, 20));

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should get books by status")
        void shouldGetBooksByStatus() {
            // Arrange
            Page<Book> bookPage = new PageImpl<>(List.of(testBook), PageRequest.of(0, 20), 1);
            when(bookRepository.findByStatusAndDeletedFalse(eq(BookStatus.AVAILABLE), any(Pageable.class)))
                    .thenReturn(bookPage);
            when(bookMapper.toResponse(testBook)).thenReturn(validBookResponse);

            // Act
            Page<BookResponse> response = bookService.getBooksByStatus(BookStatus.AVAILABLE, PageRequest.of(0, 20));

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should get available books")
        void shouldGetAvailableBooks() {
            // Arrange
            Page<Book> availableBooks = new PageImpl<>(List.of(testBook), PageRequest.of(0, 20), 1);
            when(bookRepository.findAvailableBooks(any(Pageable.class)))
                    .thenReturn(availableBooks);
            when(bookMapper.toResponse(testBook)).thenReturn(validBookResponse);

            // Act
            Page<BookResponse> response = bookService.getAvailableBooks(PageRequest.of(0, 20));

            // Assert
            assertThat(response).isNotNull();
            verify(bookRepository, times(1)).findAvailableBooks(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Category Management Tests")
    class CategoryManagementTests {

        @Test
        @DisplayName("Should add categories to book successfully")
        void shouldAddCategoriesToBook() {
            // Arrange
            Set<Long> categoryIds = TestFixtures.createCategoryIds(1L);
            Book bookWithCategories = TestFixtures.createValidBook();

            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));
            when(categoryRepository.findAllById(categoryIds)).thenReturn(List.of(testCategory));
            when(bookRepository.save(any(Book.class))).thenReturn(bookWithCategories);
            when(bookMapper.toResponse(bookWithCategories)).thenReturn(validBookResponse);

            // Act
            BookResponse response = bookService.addCategoriesToBook(1L, categoryIds);

            // Assert
            assertThat(response).isNotNull();
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should remove categories from book successfully")
        void shouldRemoveCategoriesFromBook() {
            // Arrange
            Set<Long> categoryIds = TestFixtures.createCategoryIds(1L);
            testBook.setCategories(new java.util.HashSet<>(Set.of(testCategory)));

            when(bookRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);
            when(bookMapper.toResponse(testBook)).thenReturn(validBookResponse);

            // Act
            BookResponse response = bookService.removeCategoriesFromBook(1L, categoryIds);

            // Assert
            assertThat(response).isNotNull();
            verify(bookRepository, times(1)).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get total book count")
        void shouldGetTotalBookCount() {
            // Arrange
            when(bookRepository.countActiveBooks()).thenReturn(50L);

            // Act
            long count = bookService.getTotalBookCount();

            // Assert
            assertThat(count).isEqualTo(50L);
            verify(bookRepository, times(1)).countActiveBooks();
        }

        @Test
        @DisplayName("Should get book count by status")
        void shouldGetBookCountByStatus() {
            // Arrange
            when(bookRepository.countByStatusAndDeletedFalse(BookStatus.AVAILABLE)).thenReturn(45L);

            // Act
            long count = bookService.getBookCountByStatus(BookStatus.AVAILABLE);

            // Assert
            assertThat(count).isEqualTo(45L);
        }
    }
}

