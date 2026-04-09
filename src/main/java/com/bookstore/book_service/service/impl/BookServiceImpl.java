package com.bookstore.book_service.service.impl;

import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.exception.ResourceNotFoundException;
import com.bookstore.book_service.mapper.BookMapper;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.repository.BookRepository;
import com.bookstore.book_service.service.BookService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

private final BookRepository bookRepository;

private final BookMapper  bookMapper;


    @Override
    public Page<BookResponse> findAllBooks(Pageable pageable) {

        log.info("Fetching all books with pagination:{} ",pageable);

        Page<Book> books = bookRepository.findByDeletedFalse(pageable);

        return  books.map(bookMapper::toResponse);
// return bookRepository.findAll().stream()
//         .map(bookMapper::toResponse)
//         .toList();

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
    public Page<BookResponse> searchBooks(String keyword, Pageable pageable) {
        log.info("search for  books  with keyword:{} and pagination:{} ", keyword, pageable);


        Page<Book> books = bookRepository.searchByKeyword(keyword,pageable);

        return books.map(bookMapper::toResponse);


    }
}
