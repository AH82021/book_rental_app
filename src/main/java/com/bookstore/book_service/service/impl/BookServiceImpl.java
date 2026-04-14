package com.bookstore.book_service.service.impl;

import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.exception.ResourceNotFoundException;
import com.bookstore.book_service.mapper.BookMapper;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.repository.BookRepository;
import com.bookstore.book_service.service.BookService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
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
    // helper method
    private Book findBookByIdOrThrow(Long Id){
        return bookRepository.findByIdAndDeletedFalse(Id)
                .orElseThrow(()-> new ResourceNotFoundException("Book not found with Id"+ Id));

    }

}
