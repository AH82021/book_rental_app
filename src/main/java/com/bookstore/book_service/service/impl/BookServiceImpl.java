package com.bookstore.book_service.service.impl;

import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.mapper.BookMapper;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.repository.BookRepository;
import com.bookstore.book_service.service.BookService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

private final BookRepository bookRepository;

private final BookMapper  bookMapper;


    @Override
    public List<BookResponse> findAllBooks() {
 return bookRepository.findAll().stream()
         .map(bookMapper::toResponse)
         .toList();

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

     Book book =   bookRepository.findById(id).orElseThrow();

     return bookMapper.toResponse(book);

    }
}
