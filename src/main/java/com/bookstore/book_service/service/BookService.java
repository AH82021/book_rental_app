package com.bookstore.book_service.service;


import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.repository.BookRepository;

import java.util.List;

public interface BookService {
    List<BookResponse> findAllBooks();

    BookResponse createBook(BookCreateRequest bookCreateRequest);

    BookResponse getBookById(Long id);

}
