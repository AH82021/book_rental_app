package com.bookstore.book_service.service;


import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.BookUpdateRequest;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {
    Page<BookResponse> findAllBooks(Pageable pageable);

    BookResponse createBook(BookCreateRequest bookCreateRequest);

    BookResponse getBookById(Long id);


    Page<BookResponse>   searchBooks(String keyword, Pageable pageable);

    Page<BookResponse> advancedSearch(String title, String author, String isbn,
                                   Long categoryId, BookStatus status, BigDecimal minPrice,
                                   BigDecimal maxPrice, String publisher, String language,
                                   Boolean available, Pageable pageable);

    void deleteBookById(Long Id);

    BookResponse updateBook(Long id, BookUpdateRequest request);

    Page<BookResponse> getBookByStatus (BookStatus status, Pageable pageable);

}
