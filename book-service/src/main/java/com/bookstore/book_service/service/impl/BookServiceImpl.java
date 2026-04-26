package com.bookstore.book_service.service.impl;

import com.bookstore.book_service.config.CacheConfig;
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
import com.bookstore.book_service.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_FEATURED_BOOKS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_LATEST_BOOKS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public BookResponse createBook(BookCreateRequest request) {
        log.debug("Creating book with title: {}", request.getTitle());

        // Check if ISBN already exists
        if (request.getIsbn() != null && bookRepository.existsByIsbnAndDeletedFalse(request.getIsbn())) {
            throw new DuplicateResourceException("Book with ISBN " + request.getIsbn() + " already exists");
        }

        // Validate categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            validateCategories(request.getCategoryIds());
        }

        // Set available copies to total copies if not provided
        if (request.getAvailableCopies() == null) {
            request.setAvailableCopies(request.getTotalCopies());
        }

        // Validate available copies doesn't exceed total copies
        if (request.getAvailableCopies() > request.getTotalCopies()) {
            throw new InvalidRequestException("Available copies cannot exceed total copies");
        }

        Book book = bookMapper.toEntity(request);

        // Load and set categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = categoryRepository.findAllById(request.getCategoryIds())
                    .stream().collect(Collectors.toSet());
            book.setCategories(categories);
        }

        Book savedBook = bookRepository.save(book);
        log.info("Created book with ID: {} and title: {}", savedBook.getId(), savedBook.getTitle());

        return bookMapper.toResponse(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_BOOKS, key = "#id")
    public BookResponse getBookById(Long id) {
        log.debug("Fetching book with ID: {}", id);

        Book book = findBookByIdOrThrow(id);
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_BOOKS_BY_ISBN, key = "#isbn")
    public BookResponse getBookByIsbn(String isbn) {
        log.debug("Fetching book with ISBN: {}", isbn);

        Book book = bookRepository.findByIsbnAndDeletedFalse(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));

        return bookMapper.toResponse(book);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_BOOKS, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_BOOKS_BY_ISBN, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_FEATURED_BOOKS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_LATEST_BOOKS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        log.debug("Updating book with ID: {}", id);

        Book existingBook = findBookByIdOrThrow(id);

        // Validate categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            validateCategories(request.getCategoryIds());
        }

        // Validate inventory constraints
        if (request.getTotalCopies() != null && request.getAvailableCopies() != null) {
            if (request.getAvailableCopies() > request.getTotalCopies()) {
                throw new InvalidRequestException("Available copies cannot exceed total copies");
            }
        } else if (request.getTotalCopies() != null && request.getTotalCopies() < existingBook.getAvailableCopies()) {
            throw new InvalidRequestException("Total copies cannot be less than current available copies");
        } else if (request.getAvailableCopies() != null && request.getAvailableCopies() > existingBook.getTotalCopies()) {
            throw new InvalidRequestException("Available copies cannot exceed total copies");
        }

        // Update basic fields
        bookMapper.updateEntity(existingBook, request);

        // Update categories if provided
        if (request.getCategoryIds() != null) {
            if (request.getCategoryIds().isEmpty()) {
                existingBook.getCategories().clear();
            } else {
                Set<Category> categories = categoryRepository.findAllById(request.getCategoryIds())
                        .stream().collect(Collectors.toSet());
                existingBook.setCategories(categories);
            }
        }

        Book updatedBook = bookRepository.save(existingBook);
        log.info("Updated book with ID: {}", updatedBook.getId());

        return bookMapper.toResponse(updatedBook);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_BOOKS, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_BOOKS_BY_ISBN, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_FEATURED_BOOKS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_LATEST_BOOKS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public void deleteBookById(Long id) {
        log.debug("Deleting book with ID: {}", id);

        Book book = findBookByIdOrThrow(id);
        book.softDelete();
        bookRepository.save(book);

        log.info("Deleted book with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> findAllBooks(Pageable pageable) {
        log.debug("Fetching all books with pagination: {}", pageable);

        Page<Book> books = bookRepository.findByDeletedFalse(pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(String keyword, Pageable pageable) {
        log.debug("Searching books with keyword: {} and pagination: {}", keyword, pageable);

        Page<Book> books = bookRepository.searchByKeyword(keyword, pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooksAdvanced(String title, String author, String isbn,
                                                  Long categoryId, BookStatus status,
                                                  BigDecimal minPrice, BigDecimal maxPrice,
                                                  String publisher, String language,
                                                  Boolean available, Pageable pageable) {
        log.debug("Advanced search with multiple criteria");

        Page<Book> books = bookRepository.searchBooks(title, author, isbn, categoryId, status,
                minPrice, maxPrice, publisher, language, available, pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getBooksByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching books by category ID: {}", categoryId);

        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with ID: " + categoryId);
        }

        Page<Book> books = bookRepository.findByCategoryId(categoryId, pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getBooksByStatus(BookStatus status, Pageable pageable) {
        log.debug("Fetching books by status: {}", status);

        Page<Book> books = bookRepository.findByStatusAndDeletedFalse(status, pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getAvailableBooks(Pageable pageable) {
        log.debug("Fetching available books");

        Page<Book> books = bookRepository.findAvailableBooks(pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getLowStockBooks(int threshold, Pageable pageable) {
        log.debug("Fetching low stock books with threshold: {}", threshold);

        Page<Book> books = bookRepository.findLowStockBooks(threshold, pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_BOOKS, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_FEATURED_BOOKS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public BookResponse updateInventory(Long id, Integer totalCopies, Integer availableCopies) {
        log.debug("Updating inventory for book ID: {}", id);

        Book book = findBookByIdOrThrow(id);

        if (availableCopies > totalCopies) {
            throw new InvalidRequestException("Available copies cannot exceed total copies");
        }

        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(availableCopies);

        Book updatedBook = bookRepository.save(book);
        log.info("Updated inventory for book ID: {}", id);

        return bookMapper.toResponse(updatedBook);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_BOOKS, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_FEATURED_BOOKS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public BookResponse reserveCopies(Long id, Integer quantity) {
        log.debug("Reserving {} copies for book ID: {}", quantity, id);

        Book book = findBookByIdOrThrow(id);

        if (book.getAvailableCopies() < quantity) {
            throw new InsufficientInventoryException("Not enough copies available. Available: " +
                    book.getAvailableCopies() + ", Requested: " + quantity);
        }

        book.setAvailableCopies(book.getAvailableCopies() - quantity);

        Book updatedBook = bookRepository.save(book);
        log.info("Reserved {} copies for book ID: {}", quantity, id);

        return bookMapper.toResponse(updatedBook);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_BOOKS, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_FEATURED_BOOKS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public BookResponse releaseCopies(Long id, Integer quantity) {
        log.debug("Releasing {} copies for book ID: {}", quantity, id);

        Book book = findBookByIdOrThrow(id);

        int newAvailableCopies = book.getAvailableCopies() + quantity;
        if (newAvailableCopies > book.getTotalCopies()) {
            throw new InvalidRequestException("Cannot release more copies than total copies. " +
                    "Total: " + book.getTotalCopies() + ", After release: " + newAvailableCopies);
        }

        book.setAvailableCopies(newAvailableCopies);

        Book updatedBook = bookRepository.save(book);
        log.info("Released {} copies for book ID: {}", quantity, id);

        return bookMapper.toResponse(updatedBook);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_BOOKS, key = "#bookId"),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public BookResponse addCategoriesToBook(Long bookId, Set<Long> categoryIds) {
        log.debug("Adding categories to book ID: {}", bookId);

        Book book = findBookByIdOrThrow(bookId);
        validateCategories(categoryIds);

        Set<Category> categoriesToAdd = categoryRepository.findAllById(categoryIds)
                .stream().collect(Collectors.toSet());

        book.getCategories().addAll(categoriesToAdd);

        Book updatedBook = bookRepository.save(book);
        log.info("Added {} categories to book ID: {}", categoryIds.size(), bookId);

        return bookMapper.toResponse(updatedBook);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_BOOKS, key = "#bookId"),
            @CacheEvict(value = CacheConfig.CACHE_BOOK_COUNTS, allEntries = true)
    })
    public BookResponse removeCategoriesFromBook(Long bookId, Set<Long> categoryIds) {
        log.debug("Removing categories from book ID: {}", bookId);

        Book book = findBookByIdOrThrow(bookId);

        Set<Category> categoriesToRemove = book.getCategories().stream()
                .filter(category -> categoryIds.contains(category.getId()))
                .collect(Collectors.toSet());

        book.getCategories().removeAll(categoriesToRemove);

        Book updatedBook = bookRepository.save(book);
        log.info("Removed {} categories from book ID: {}", categoriesToRemove.size(), bookId);

        return bookMapper.toResponse(updatedBook);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_FEATURED_BOOKS, key = "#limit")
    public List<BookResponse> getFeaturedBooks(int limit) {
        log.debug("Fetching {} featured books", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Book> books = bookRepository.findPopularBooks(pageable);
        return books.stream().map(bookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_LATEST_BOOKS, key = "#limit")
    public List<BookResponse> getLatestBooks(int limit) {
        log.debug("Fetching {} latest books", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Book> books = bookRepository.findLatestBooks(pageable);
        return books.stream().map(bookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbnAndDeletedFalse(isbn);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_BOOK_COUNTS, key = "'total'")
    public long getTotalBookCount() {
        return bookRepository.countActiveBooks();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_BOOK_COUNTS, key = "#status")
    public long getBookCountByStatus(BookStatus status) {
        return bookRepository.countByStatusAndDeletedFalse(status);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_BOOK_COUNTS, key = "'cat-' + #categoryId")
    public long getBookCountByCategory(Long categoryId) {
        return bookRepository.countBooksByCategory(categoryId);
    }

    // Helper methods
    private Book findBookByIdOrThrow(Long id) {
        return bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));
    }

    private void validateCategories(Set<Long> categoryIds) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            Set<Long> foundIds = categories.stream().map(Category::getId).collect(Collectors.toSet());
            Set<Long> notFoundIds = categoryIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());
            throw new ResourceNotFoundException("Categories not found with IDs: " + notFoundIds);
        }
    }
}
