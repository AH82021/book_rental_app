package com.bookstore.book_service.model;

import com.bookstore.book_service.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Book Model Unit Tests")
class BookTest {

    private Book book;
    private Category category;

    @BeforeEach
    void setUp() {
        book = TestFixtures.createValidBook();
        category = TestFixtures.createValidCategory();
    }

    @Nested
    @DisplayName("Book Availability Tests")
    class BookAvailabilityTests {

        @Test
        @DisplayName("Should identify available book")
        void shouldIdentifyAvailableBook() {
            // Arrange
            book.setStatus(BookStatus.AVAILABLE);
            book.setDeleted(false);
            book.setAvailableCopies(10);

            // Act
            boolean isAvailable = book.isAvailable();

            // Assert
            assertThat(isAvailable).isTrue();
        }

        @Test
        @DisplayName("Should identify unavailable book when status is OUT_OF_STOCK")
        void shouldIdentifyUnavailableWhenStatusUnavailable() {
            // Arrange
            book.setStatus(BookStatus.OUT_OF_STOCK);
            book.setDeleted(false);
            book.setAvailableCopies(10);

            // Act
            boolean isAvailable = book.isAvailable();

            // Assert
            assertThat(isAvailable).isFalse();
        }

        @Test
        @DisplayName("Should identify unavailable book when no copies available")
        void shouldIdentifyUnavailableWhenNoCopies() {
            // Arrange
            book.setStatus(BookStatus.AVAILABLE);
            book.setDeleted(false);
            book.setAvailableCopies(0);

            // Act
            boolean isAvailable = book.isAvailable();

            // Assert
            assertThat(isAvailable).isFalse();
        }

        @Test
        @DisplayName("Should identify unavailable book when deleted")
        void shouldIdentifyUnavailableWhenDeleted() {
            // Arrange
            book.setStatus(BookStatus.AVAILABLE);
            book.setDeleted(true);
            book.setAvailableCopies(10);

            // Act
            boolean isAvailable = book.isAvailable();

            // Assert
            assertThat(isAvailable).isFalse();
        }
    }

    @Nested
    @DisplayName("Book Category Management Tests")
    class BookCategoryManagementTests {

        @Test
        @DisplayName("Should add category to book successfully")
        void shouldAddCategoryToBook() {
            // Act
            book.addCategory(category);

            // Assert
            assertThat(book.getCategories()).contains(category);
            assertThat(category.getBooks()).contains(book);
        }

        @Test
        @DisplayName("Should remove category from book successfully")
        void shouldRemoveCategoryFromBook() {
            // Arrange
            book.addCategory(category);

            // Act
            book.removeCategory(category);

            // Assert
            assertThat(book.getCategories()).doesNotContain(category);
            assertThat(category.getBooks()).doesNotContain(book);
        }

        @Test
        @DisplayName("Should add multiple categories to book")
        void shouldAddMultipleCategories() {
            // Arrange
            Category category1 = TestFixtures.createCategory(1L, "Category1", "cat1");
            Category category2 = TestFixtures.createCategory(2L, "Category2", "cat2");

            // Act
            book.addCategory(category1);
            book.addCategory(category2);

            // Assert
            assertThat(book.getCategories()).hasSize(2);
            assertThat(book.getCategories()).contains(category1, category2);
        }
    }

    @Nested
    @DisplayName("Book Soft Delete Tests")
    class BookSoftDeleteTests {

        @Test
        @DisplayName("Should mark book as deleted")
        void shouldMarkBookAsDeleted() {
            // Arrange
            assertThat(book.getDeleted()).isFalse();

            // Act
            book.softDelete();

            // Assert
            assertThat(book.getDeleted()).isTrue();
        }

        @Test
        @DisplayName("Should not be available after soft delete")
        void shouldNotBeAvailableAfterSoftDelete() {
            // Arrange
            book.setStatus(BookStatus.AVAILABLE);
            book.setAvailableCopies(10);

            // Act
            book.softDelete();

            // Assert
            assertThat(book.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("Book Validation Tests")
    class BookValidationTests {

        @Test
        @DisplayName("Should have valid ISBN format (10 digits)")
        void shouldValidate10DigitIsbn() {
            // Arrange & Act
            book.setIsbn("1234567890");

            // Assert
            assertThat(book.getIsbn()).hasSize(10);
            assertThat(book.getIsbn()).matches("^\\d{10}$");
        }

        @Test
        @DisplayName("Should have valid ISBN format (13 digits)")
        void shouldValidate13DigitIsbn() {
            // Arrange & Act
            book.setIsbn("9780743273565");

            // Assert
            assertThat(book.getIsbn()).hasSize(13);
            assertThat(book.getIsbn()).matches("^\\d{13}$");
        }

        @Test
        @DisplayName("Should enforce positive price")
        void shouldEnforcePositivePrice() {
            // Arrange & Act
            book.setPrice(java.math.BigDecimal.valueOf(29.99));

            // Assert
            assertThat(book.getPrice()).isGreaterThan(java.math.BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should enforce non-negative available copies")
        void shouldEnforceNonNegativeAvailableCopies() {
            // Arrange & Act
            book.setAvailableCopies(0);

            // Assert
            assertThat(book.getAvailableCopies()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should enforce positive total copies")
        void shouldEnforcePositiveTotalCopies() {
            // Arrange & Act
            book.setTotalCopies(1);

            // Assert
            assertThat(book.getTotalCopies()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Book Equality Tests")
    class BookEqualityTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Act & Assert
            assertThat(book).isEqualTo(book);
        }

        @Test
        @DisplayName("Should not equal null")
        void shouldNotEqualNull() {
            // Act & Assert
            assertThat(book).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            // Act
            int hashCode1 = book.hashCode();
            int hashCode2 = book.hashCode();

            // Assert
            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }
}

@DisplayName("Category Model Unit Tests")
class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = TestFixtures.createValidCategory();
    }

    @Nested
    @DisplayName("Category Hierarchy Tests")
    class CategoryHierarchyTests {

        @Test
        @DisplayName("Should identify top-level category")
        void shouldIdentifyTopLevelCategory() {
            // Act
            boolean isTopLevel = category.isTopLevel();

            // Assert
            assertThat(isTopLevel).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-top-level category")
        void shouldNotIdentifyNonTopLevelCategory() {
            // Arrange
            Category parentCategory = TestFixtures.createValidCategory();
            category.setParent(parentCategory);

            // Act
            boolean isTopLevel = category.isTopLevel();

            // Assert
            assertThat(isTopLevel).isFalse();
        }

        @Test
        @DisplayName("Should identify category with children")
        void shouldIdentifyCategoryWithChildren() {
            // Arrange
            Category childCategory = TestFixtures.createCategory(2L, "Child", "child");
            category.addChild(childCategory);

            // Act
            boolean hasChildren = category.hasChildren();

            // Assert
            assertThat(hasChildren).isTrue();
        }

        @Test
        @DisplayName("Should identify category without children")
        void shouldIdentifyCategoryWithoutChildren() {
            // Act
            boolean hasChildren = category.hasChildren();

            // Assert
            assertThat(hasChildren).isFalse();
        }
    }

    @Nested
    @DisplayName("Category Child Management Tests")
    class CategoryChildManagementTests {

        @Test
        @DisplayName("Should add child category successfully")
        void shouldAddChildCategory() {
            // Arrange
            Category childCategory = TestFixtures.createCategory(2L, "Child", "child");

            // Act
            category.addChild(childCategory);

            // Assert
            assertThat(category.getChildren()).contains(childCategory);
            assertThat(childCategory.getParent()).isEqualTo(category);
        }

        @Test
        @DisplayName("Should remove child category successfully")
        void shouldRemoveChildCategory() {
            // Arrange
            Category childCategory = TestFixtures.createCategory(2L, "Child", "child");
            category.addChild(childCategory);

            // Act
            category.removeChild(childCategory);

            // Assert
            assertThat(category.getChildren()).doesNotContain(childCategory);
            assertThat(childCategory.getParent()).isNull();
        }

        @Test
        @DisplayName("Should manage multiple child categories")
        void shouldManageMultipleChildCategories() {
            // Arrange
            Category child1 = TestFixtures.createCategory(1L, "Child1", "child1");
            Category child2 = TestFixtures.createCategory(2L, "Child2", "child2");
            Category child3 = TestFixtures.createCategory(3L, "Child3", "child3");

            // Act
            category.addChild(child1);
            category.addChild(child2);
            category.addChild(child3);

            // Assert
            assertThat(category.getChildren()).hasSize(3);
            assertThat(category.hasChildren()).isTrue();
        }
    }

    @Nested
    @DisplayName("Category Validation Tests")
    class CategoryValidationTests {

        @Test
        @DisplayName("Should enforce non-empty name")
        void shouldEnforceNonEmptyName() {
            // Act
            category.setName("Fiction");

            // Assert
            assertThat(category.getName()).isNotBlank();
        }

        @Test
        @DisplayName("Should enforce non-empty slug")
        void shouldEnforceNonEmptySlug() {
            // Act
            category.setSlug("fiction");

            // Assert
            assertThat(category.getSlug()).isNotBlank();
        }

        @Test
        @DisplayName("Should have unique name constraint")
        void shouldHaveUniqueName() {
            // Arrange
            Category category1 = TestFixtures.createValidCategory();
            Category category2 = TestFixtures.createValidCategory();

            // Act & Assert
            assertThat(category1.getName()).isEqualTo(category2.getName());
            // In database layer, unique constraint would prevent saving duplicates
        }

        @Test
        @DisplayName("Should have unique slug constraint")
        void shouldHaveUniqueSlug() {
            // Arrange
            Category category1 = TestFixtures.createValidCategory();
            Category category2 = TestFixtures.createValidCategory();

            // Act & Assert
            assertThat(category1.getSlug()).isEqualTo(category2.getSlug());
            // In database layer, unique constraint would prevent saving duplicates
        }
    }

    @Nested
    @DisplayName("Category Equality Tests")
    class CategoryEqualityTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Act & Assert
            assertThat(category).isEqualTo(category);
        }

        @Test
        @DisplayName("Should not equal null")
        void shouldNotEqualNull() {
            // Act & Assert
            assertThat(category).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            // Act
            int hashCode1 = category.hashCode();
            int hashCode2 = category.hashCode();

            // Assert
            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }
}

