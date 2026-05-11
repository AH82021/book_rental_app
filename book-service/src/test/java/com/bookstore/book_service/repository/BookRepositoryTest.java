package com.bookstore.book_service.repository;

import com.bookstore.book_service.TestFixtures;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Book Repository Tests")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Book testBook;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        categoryRepository.deleteAll();

        testBook = TestFixtures.createValidBook();
        testBook.setId(null);

        testCategory = Category.builder()
                .name("Test Category")
                .slug("test-cat")
                .description("Test category")
                .children(new HashSet<>())
                .books(new HashSet<>())
                .build();
        categoryRepository.save(testCategory);
    }

    @Nested
    @DisplayName("Book Creation and Retrieval Tests")
    class BookCreationAndRetrievalTests {

        @Test
        @DisplayName("Should save and retrieve book successfully")
        void shouldSaveAndRetrieveBook() {
            // Arrange & Act
            Book savedBook = bookRepository.save(testBook);

            // Assert
            assertThat(savedBook.getId()).isNotNull();
            Optional<Book> retrievedBook = bookRepository.findById(savedBook.getId());
            assertThat(retrievedBook).isPresent();
            assertThat(retrievedBook.get().getTitle()).isEqualTo("The Great Gatsby");
        }

        @Test
        @DisplayName("Should retrieve active books only")
        void shouldRetrieveActiveBooksOnly() {
            // Arrange
            Book activeBook = TestFixtures.createValidBook();
            activeBook.setId(null);
            activeBook.setDeleted(false);
            bookRepository.save(activeBook);

            Book deletedBook = TestFixtures.createValidBook();
            deletedBook.setId(null);
            deletedBook.setIsbn("9999999999999");
            deletedBook.setDeleted(true);
            bookRepository.save(deletedBook);

            // Act
            long activeCount = bookRepository.countActiveBooks();

            // Assert
            assertThat(activeCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("ISBN Tests")
    class IsbnTests {

        @Test
        @DisplayName("Should find book by ISBN when not deleted")
        void shouldFindBookByIsbn() {
            // Arrange
            bookRepository.save(testBook);

            // Act
            Optional<Book> foundBook = bookRepository.findByIsbnAndDeletedFalse("9780743273565");

            // Assert
            assertThat(foundBook).isPresent();
            assertThat(foundBook.get().getTitle()).isEqualTo("The Great Gatsby");
        }

        @Test
        @DisplayName("Should not find deleted book by ISBN")
        void shouldNotFindDeletedBookByIsbn() {
            // Arrange
            testBook.setDeleted(true);
            bookRepository.save(testBook);

            // Act
            Optional<Book> foundBook = bookRepository.findByIsbnAndDeletedFalse("9780743273565");

            // Assert
            assertThat(foundBook).isEmpty();
        }

        @Test
        @DisplayName("Should check if ISBN exists")
        void shouldCheckIfIsbnExists() {
            // Arrange
            bookRepository.save(testBook);

            // Act
            boolean exists = bookRepository.existsByIsbnAndDeletedFalse("9780743273565");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should not find non-existent ISBN")
        void shouldNotFindNonExistentIsbn() {
            // Arrange & Act
            boolean exists = bookRepository.existsByIsbnAndDeletedFalse("1111111111111");

            // Assert
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Book Status Tests")
    class BookStatusTests {

        @Test
        @DisplayName("Should find books by status")
        void shouldFindBooksByStatus() {
            // Arrange
            Book availableBook = TestFixtures.createValidBook();
            availableBook.setId(null);
            availableBook.setStatus(BookStatus.AVAILABLE);
            availableBook.setDeleted(false);
            bookRepository.save(availableBook);

            Book unavailableBook = TestFixtures.createValidBook();
            unavailableBook.setId(null);
            unavailableBook.setIsbn("9999999999999");
            unavailableBook.setStatus(BookStatus.DISCONTINUED);
            unavailableBook.setDeleted(false);
            bookRepository.save(unavailableBook);

            // Act
            Page<Book> availableBooks = bookRepository.findByStatusAndDeletedFalse(
                    BookStatus.AVAILABLE,
                    PageRequest.of(0, 20)
            );

            // Assert
            assertThat(availableBooks.getContent()).hasSize(1);
            assertThat(availableBooks.getContent().get(0).getStatus()).isEqualTo(BookStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Should count books by status")
        void shouldCountBooksByStatus() {
            // Arrange
            Book book1 = TestFixtures.createValidBook();
            book1.setId(null);
            book1.setStatus(BookStatus.AVAILABLE);
            Book book2 = TestFixtures.createValidBook();
            book2.setId(null);
            book2.setIsbn("9999999999999");
            book2.setStatus(BookStatus.AVAILABLE);

            bookRepository.save(book1);
            bookRepository.save(book2);

            // Act
            long count = bookRepository.countByStatusAndDeletedFalse(BookStatus.AVAILABLE);

            // Assert
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should retrieve books with pagination")
        void shouldRetrieveBooksWithPagination() {
            // Arrange
            for (int i = 0; i < 5; i++) {
                Book book = TestFixtures.createBook((long)i, "Book " + i, "Author " + i,
                        "978074327356" + i, 100, 100);
                book.setId(null);
                book.setDeleted(false);
                bookRepository.save(book);
            }

            // Act
            Page<Book> page1 = bookRepository.findByDeletedFalse(PageRequest.of(0, 2));
            Page<Book> page2 = bookRepository.findByDeletedFalse(PageRequest.of(1, 2));

            // Assert
            assertThat(page1.getTotalElements()).isEqualTo(5);
            assertThat(page1.getTotalPages()).isEqualTo(3);
            assertThat(page1.getContent()).hasSize(2);
            assertThat(page2.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Inventory Tests")
    class InventoryTests {

        @Test
        @DisplayName("Should find available books")
        void shouldFindAvailableBooks() {
            // Arrange
            Book availableBook = TestFixtures.createValidBook();
            availableBook.setId(null);
            availableBook.setStatus(BookStatus.AVAILABLE);
            availableBook.setAvailableCopies(10);
            availableBook.setDeleted(false);
            bookRepository.save(availableBook);

            Book unavailableBook = TestFixtures.createValidBook();
            unavailableBook.setId(null);
            unavailableBook.setIsbn("9999999999999");
            unavailableBook.setAvailableCopies(0);
            unavailableBook.setDeleted(false);
            bookRepository.save(unavailableBook);

            // Act
            Page<Book> available = bookRepository.findAvailableBooks(PageRequest.of(0, 20));

            // Assert
            assertThat(available.getContent()).hasSize(1);
            assertThat(available.getContent().get(0).getAvailableCopies()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should find books with low stock")
        void shouldFindBooksWithLowStock() {
            // Arrange
            Book lowStockBook = TestFixtures.createValidBook();
            lowStockBook.setId(null);
            lowStockBook.setTotalCopies(10);
            lowStockBook.setAvailableCopies(3);
            lowStockBook.setDeleted(false);
            bookRepository.save(lowStockBook);

            Book highStockBook = TestFixtures.createValidBook();
            highStockBook.setId(null);
            highStockBook.setIsbn("9999999999999");
            highStockBook.setTotalCopies(100);
            highStockBook.setAvailableCopies(50);
            highStockBook.setDeleted(false);
            bookRepository.save(highStockBook);

            // Act
            Page<Book> lowStock = bookRepository.findLowStockBooks(5, PageRequest.of(0, 20));

            // Assert
            assertThat(lowStock.getContent()).hasSize(1);
            assertThat(lowStock.getContent().get(0).getAvailableCopies()).isLessThan(5);
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should search books by keyword")
        void shouldSearchBooksByKeyword() {
            // Arrange
            bookRepository.save(testBook);

            Book otherBook = TestFixtures.createBook(null, "To Kill a Mockingbird", "Harper Lee",
                    "9780061120084", 100, 100);
            bookRepository.save(otherBook);

            // Act
            Page<Book> results = bookRepository.searchByKeyword("Gatsby", PageRequest.of(0, 20));

            // Assert
            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getTitle()).contains("Gatsby");
        }

        @Test
        @DisplayName("Should search by title")
        void shouldSearchByTitle() {
            // Arrange
            testBook.setId(null);
            bookRepository.save(testBook);

            // Act
            Page<Book> results = bookRepository.findByTitleContainingIgnoreCaseAndDeletedFalse("Gatsby", PageRequest.of(0, 20));

            // Assert
            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getTitle()).contains("Gatsby");
        }

        @Test
        @DisplayName("Should search by author")
        void shouldSearchByAuthor() {
            // Arrange
            testBook.setId(null);
            bookRepository.save(testBook);

            // Act
            Page<Book> results = bookRepository.findByAuthorContainingIgnoreCaseAndDeletedFalse("Fitzgerald", PageRequest.of(0, 20));

            // Assert
            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getAuthor()).contains("Fitzgerald");
        }
    }

    @Nested
    @DisplayName("Category Tests")
    class CategoryTests {

        @Test
        @DisplayName("Should find books by category")
        void shouldFindBooksByCategory() {
            // Arrange
            testBook.setCategories(java.util.Set.of(testCategory));
            testBook.setId(null);
            bookRepository.save(testBook);

            // Act
            Page<Book> results = bookRepository.findByCategoryId(testCategory.getId(), PageRequest.of(0, 20));

            // Assert
            assertThat(results.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should count books in category")
        void shouldCountBooksInCategory() {
            // Arrange
            testBook.setCategories(java.util.Set.of(testCategory));
            testBook.setId(null);
            bookRepository.save(testBook);

            // Act
            long count = bookRepository.countBooksByCategory(testCategory.getId());

            // Assert
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Sorting Tests")
    class SortingTests {

        @Test
        @DisplayName("Should get books sorted by creation date descending")
        void shouldGetBooksSortedByCreationDate() {
            // Arrange
            Book book1 = TestFixtures.createBook(null, "Book 1", "Author 1", "1111111111111", 100, 100);
            Book book2 = TestFixtures.createBook(null, "Book 2", "Author 2", "2222222222222", 100, 100);
            Book book3 = TestFixtures.createBook(null, "Book 3", "Author 3", "3333333333333", 100, 100);

            bookRepository.save(book1);
            bookRepository.save(book2);
            bookRepository.save(book3);

            // Act
            Page<Book> sortedBooks = bookRepository.findByDeletedFalse(
                    PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("createdAt").descending())
            );

            // Assert
            assertThat(sortedBooks.getContent()).hasSize(3);
        }
    }
}

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Category Repository Tests")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        testCategory = TestFixtures.createValidCategory();
        testCategory.setId(null);
    }

    @Nested
    @DisplayName("Category Creation and Retrieval Tests")
    class CategoryCreationAndRetrievalTests {

        @Test
        @DisplayName("Should save and retrieve category successfully")
        void shouldSaveAndRetrieveCategory() {
            // Act
            Category savedCategory = categoryRepository.save(testCategory);

            // Assert
            assertThat(savedCategory.getId()).isNotNull();
            Optional<Category> retrieved = categoryRepository.findById(savedCategory.getId());
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getName()).isEqualTo("Fiction");
        }
    }

    @Nested
    @DisplayName("Category Name and Slug Tests")
    class CategoryNameAndSlugTests {

        @Test
        @DisplayName("Should find category by name")
        void shouldFindCategoryByName() {
            // Arrange
            categoryRepository.save(testCategory);

            // Act
            boolean exists = categoryRepository.existsByName("Fiction");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should find category by slug")
        void shouldFindCategoryBySlug() {
            // Arrange
            categoryRepository.save(testCategory);

            // Act
            Optional<Category> found = categoryRepository.findBySlug("fiction");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getSlug()).isEqualTo("fiction");
        }

        @Test
        @DisplayName("Should check if slug exists")
        void shouldCheckIfSlugExists() {
            // Arrange
            categoryRepository.save(testCategory);

            // Act
            boolean exists = categoryRepository.existsBySlug("fiction");

            // Assert
            assertThat(exists).isTrue();
        }
    }

    @Nested
    @DisplayName("Hierarchy Tests")
    class HierarchyTests {

        @Test
        @DisplayName("Should find root categories")
        void shouldFindRootCategories() {
            // Arrange
            Category rootCategory1 = TestFixtures.createValidCategory();
            rootCategory1.setId(null);
            Category rootCategory2 = TestFixtures.createCategory(null, "Non-Fiction", "non-fiction");
            rootCategory2.setId(null);

            categoryRepository.save(rootCategory1);
            categoryRepository.save(rootCategory2);

            // Act
            Page<Category> rootCategories = categoryRepository.findByParentIsNull(PageRequest.of(0, 20));

            // Assert
            assertThat(rootCategories.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("Should find subcategories by parent")
        void shouldFindSubcategoriesByParent() {
            // Arrange
            Category parent = TestFixtures.createValidCategory();
            parent.setId(null);
            Category savedParent = categoryRepository.save(parent);

            Category child1 = TestFixtures.createCategoryWithParent(null, "Sub1", "sub1", savedParent);
            Category child2 = TestFixtures.createCategoryWithParent(null, "Sub2", "sub2", savedParent);

            categoryRepository.save(child1);
            categoryRepository.save(child2);

            // Act
            Page<Category> children = categoryRepository.findByParentId(savedParent.getId(), PageRequest.of(0, 20));

            // Assert
            assertThat(children.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should search categories by name")
        void shouldSearchCategoriesByName() {
            // Arrange
            categoryRepository.save(testCategory);

            // Act
            Page<Category> results = categoryRepository.findByNameContainingIgnoreCase(
                    "Fict",
                    PageRequest.of(0, 20)
            );

            // Assert
            assertThat(results.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("Should count total categories")
        void shouldCountTotalCategories() {
            // Arrange
            categoryRepository.save(testCategory);

            // Act
            long count = categoryRepository.count();

            // Assert
            assertThat(count).isEqualTo(1);
        }
    }
}




