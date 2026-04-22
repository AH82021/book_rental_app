package com.bookstore.book_service.service.impl;

import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.BookUpdateRequest;
import com.bookstore.book_service.exception.InvalidRequestException;
import com.bookstore.book_service.exception.ResourceNotFoundException;
import com.bookstore.book_service.mapper.BookMapper;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.model.Category;
import com.bookstore.book_service.repository.BookRepository;
import com.bookstore.book_service.repository.CategoryRepository;
import com.bookstore.book_service.service.BookService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

private final BookRepository bookRepository;

private final CategoryRepository categoryRepository;

private final BookMapper  bookMapper;


    @Override
    public Page<BookResponse> findAllBooks(Pageable pageable) {

        log.info("Fetching all books with pagination:{} ",pageable);

        Page<Book> books = bookRepository.findByDeletedFalse(pageable);

        return  books.map(bookMapper::toResponse);

    }

    @Override
    public BookResponse createBook(BookCreateRequest bookCreateRequest) {

        Book book = bookMapper.toEntity(bookCreateRequest);
    bookRepository.save(book);
    return bookMapper.toResponse(book);
    }

    @Override
    public BookResponse getBookById(Long id) {
        // convert book to bookResponse

     Book book =   bookRepository.findByIdAndDeletedFalse(id).orElseThrow(()-> new ResourceNotFoundException("Book with id " + id + " not found"));

     return bookMapper.toResponse(book);

    }

    @Override
    public BookResponse getBookByIsbn(String isbn) {
        return null;
    }

    @Override
    public Page<BookResponse> getBooksByCategory(Long categoryId, Pageable pageable) {
        return null;
    }

    @Transactional
    @Override
    public BookResponse addCategoriesToBook(Long bookId, Set<Long> categoryIds) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));

        if (categories.size() != categoryIds.size()) {
            throw new ResourceNotFoundException("One or more category IDs not found");
        }

        book.getCategories().addAll(categories);

        return bookMapper.toResponse(book);
    }

    @Override
    public Page<BookResponse> getAvailableBooks(Pageable pageable) {
        return null;
    }

    @Override
    public Page<BookResponse> getLowStockBooks(int threshold, Pageable pageable) {
        return null;
    }

    @Override
    public BookResponse updateInventory(Long id, Integer totalCopies, Integer availableCopies) {
        return null;
    }

    @Override
    public BookResponse reserveCopies(Long id, Integer quantity) {
        return null;
    }

    @Override
    public BookResponse releaseCopies(Long id, Integer quantity) {
        return null;
    }

    @Override
    public BookResponse removeCategoriesFromBook(Long bookId, Set<Long> categoryIds) {
        return null;
    }

    @Override
    public List<BookResponse> getFeaturedBooks(int limit) {
        return List.of();
    }

    @Override
    public List<BookResponse> getLatestBooks(int limit) {
        log.debug("Fetching latest books with limit:{} ",limit);
            Pageable pageable = PageRequest.of(0, limit);

     List<Book> books =bookRepository.findLatestBooks(pageable);
     return books.stream()
             .map(bookMapper::toResponse)
             .toList();


    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return false;
    }

    @Override
    public long getTotalBookCount() {
        return 0;
    }

    @Override
    public long getBookCountByStatus(BookStatus status) {
        return 0;
    }

    @Override
    public long getBookCountByCategory(Long categoryId) {
        return 0;
    }


    @Transactional(readOnly = true)

    @Override
    public Page<BookResponse> searchBooks(String keyword, Pageable pageable) {
        log.info("search for  books  with keyword:{} and pagination:{} ", keyword, pageable);


        Page<Book> books = bookRepository.searchByKeyword(keyword,pageable);

        return books.map(bookMapper::toResponse);


    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> advancedSearch(String title, String author, String isbn,
                                             Long categoryId, BookStatus status, BigDecimal minPrice,
                                             BigDecimal maxPrice, String publisher, String language,
                                             Boolean available, Pageable pageable) {
        log.info("Advanced search for books with title:{}, author:{}, status:{}", title, author, status);
        Page<Book> books = bookRepository.searchBooks(
                title, author, isbn, categoryId, status,
                minPrice, maxPrice, publisher, language, available, pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    public void deleteBookById(Long Id) {
        log.debug("Deleting Book by ID:{}", Id);
        Book book = findBookByIdOrThrow(Id);
        book.softDelete();
        bookRepository.save(book);
        log.info("Deleted Book with Id:{}",Id);
    }



// Update book impl
    @Override
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

        return bookMapper.toResponse(existingBook);

    }
        @Override
        public Page<BookResponse> getBooksByStatus (BookStatus status, Pageable pageable){

            log.debug("Fetching book by status: {}", status);
            Page<Book> books = bookRepository.findByStatusAndDeletedFalse(status, pageable);
            log.info("Fetching books with pagination:{} ", books);
            return books.map(bookMapper::toResponse);
        }

        // helper method
        private Book findBookByIdOrThrow (Long Id) {
            return bookRepository.findByIdAndDeletedFalse(Id)
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found with Id" + Id));

        }





        private void validateCategories (Set < Long > categoryIds) {
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
