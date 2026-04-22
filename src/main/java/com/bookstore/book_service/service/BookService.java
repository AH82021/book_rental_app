package com.bookstore.book_service.service;


import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.BookUpdateRequest;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.model.Category;
import com.bookstore.book_service.repository.BookRepository;
import jakarta.persistence.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface BookService {
    /**
     * Get all books with pagination
     * @param pageable pagination information
     * @return page of book responses
     */
    Page<BookResponse> findAllBooks(Pageable pageable);



    /**
     * Search books by keyword (title, author, description)
     * @param keyword the search keyword
     * @param pageable pagination information
     * @return page of book responses
     */
    Page<BookResponse>   searchBooks(String keyword, Pageable pageable);

    /**
     * Advanced search with multiple criteria
     * @param title book title (partial match)
     * @param author book author (partial match)
     * @param isbn exact ISBN
     * @param categoryId category ID
     * @param status book status
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param publisher publisher name (partial match)
     * @param language book language
     * @param available availability filter
     * @param pageable pagination information
     * @return page of book responses
     */

    Page<BookResponse> advancedSearch(String title, String author, String isbn,
                                   Long categoryId, BookStatus status, BigDecimal minPrice,
                                   BigDecimal maxPrice, String publisher, String language,
                                   Boolean available, Pageable pageable);


    /**
     * Soft delete a book
     * @param Id the book ID
     */
    void deleteBookById(Long Id);

    /**
     * Update an existing book
     * @param id the book ID
     * @param request the book update request
     * @return the updated book response
     */
    BookResponse updateBook(Long id, BookUpdateRequest request);
    /**
     * Get books by status
     * @param status the book status
     * @param pageable pagination information
     * @return page of book responses
     */
    Page<BookResponse> getBooksByStatus (BookStatus status, Pageable pageable);
    /**
     * Create a new book
     * @param request the book creation request
     * @return the created book response
     */
    BookResponse createBook(BookCreateRequest request);

    /**
     * Get a book by ID
     * @param id the book ID
     * @return the book response
     */
    BookResponse getBookById(Long id);

    /**
     * Get a book by ISBN
     * @param isbn the book ISBN
     * @return the book response
     */
    BookResponse getBookByIsbn(String isbn);



    /**
     * Get books by category
     * @param categoryId the category ID
     * @param pageable pagination information
     * @return page of book responses
     */
    Page<BookResponse> getBooksByCategory(Long categoryId, Pageable pageable);



    /**
     * Get available books
     * @param pageable pagination information
     * @return page of available book responses
     */
    Page<BookResponse> getAvailableBooks(Pageable pageable);

    /**
     * Get books with low stock
     * @param threshold the stock threshold
     * @param pageable pagination information
     * @return page of low stock book responses
     */
    Page<BookResponse> getLowStockBooks(int threshold, Pageable pageable);

    /**
     * Update book inventory
     * @param id the book ID
     * @param totalCopies new total copies
     * @param availableCopies new available copies
     * @return the updated book response
     */
    BookResponse updateInventory(Long id, Integer totalCopies, Integer availableCopies);

    /**
     * Reserve copies of a book (decrease available copies)
     * @param id the book ID
     * @param quantity number of copies to reserve
     * @return the updated book response
     */
    BookResponse reserveCopies(Long id, Integer quantity);

    /**
     * Release copies of a book (increase available copies)
     * @param id the book ID
     * @param quantity number of copies to release
     * @return the updated book response
     */
    BookResponse releaseCopies(Long id, Integer quantity);

    /**
     * Add categories to a book
     * @param bookId the book ID
     * @param categoryIds set of category IDs to add
     * @return the updated book response
     */
    BookResponse addCategoriesToBook(Long bookId, Set<Long> categoryIds);

    /**
     * Remove categories from a book
     * @param bookId the book ID
     * @param categoryIds set of category IDs to remove
     * @return the updated book response
     */
    BookResponse removeCategoriesFromBook(Long bookId, Set<Long> categoryIds);

    /**
     * Get featured/popular books
     * @param limit maximum number of books to return
     * @return list of featured book responses
     */
    List<BookResponse> getFeaturedBooks(int limit);

    /**
     * Get latest books
     * @param limit maximum number of books to return
     * @return list of latest book responses
     */
    List<BookResponse> getLatestBooks(int limit);

    /**
     * Check if a book exists by ISBN
     * @param isbn the ISBN to check
     * @return true if book exists, false otherwise
     */
    boolean existsByIsbn(String isbn);

    /**
     * Get total count of active books
     * @return total count of active books
     */
    long getTotalBookCount();

    /**
     * Get count of books by status
     * @param status the book status
     * @return count of books with the given status
     */
    long getBookCountByStatus(BookStatus status);

    /**
     * Get count of books by category
     * @param categoryId the category ID
     * @return count of books in the category
     */
    long getBookCountByCategory(Long categoryId);

}
