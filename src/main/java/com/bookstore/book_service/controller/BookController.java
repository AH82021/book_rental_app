package com.bookstore.book_service.controller;


import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.service.BookService;
import com.bookstore.book_service.service.impl.BookServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// localhost:8080/api/v1/books/6
// Request > Controller => Service => Repository(Model) => DB
// Repository(Model) => Service => Controller => Response

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/books")

public class BookController {
    // Inversion of Control (IoC) => Spring Framework
    // DI : Dependency Injection : is a technique where a class receives its dependencies from an external source rather than creating them itself.
//  Types : Constructor , Setter , Field

    private final BookService bookService  ;

// DTO

    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(@PageableDefault(size = 20, sort ="title") Pageable  pageable) {

        Page<BookResponse> books = bookService.findAllBooks(pageable);
        return  ResponseEntity.ok().body(books);
    }


    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id){

        BookResponse book = bookService.getBookById(id);
        return  ResponseEntity.ok().body(book);
    }


    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid  @RequestBody BookCreateRequest  request){

  log.warn("Received request to create book: {}", request);
            BookResponse response = bookService.createBook(request);

  log.info("Book created: {}", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }


    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort ="title") Pageable  pageable) {
 log.debug("Received search request with keyword: {} and pagination: {}", keyword, pageable);
        Page<BookResponse> books = bookService.searchBooks(keyword, pageable);
        return  ResponseEntity.ok().body(books);
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<Page<BookResponse>> advancedSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BookStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        log.debug("Received advanced search request with title:{}, author:{}, status:{}", title, author, status);
        Page<BookResponse> books = bookService.advancedSearch(
                title, author, isbn, categoryId, status,
                minPrice, maxPrice, publisher, language, available, pageable
        );
        return ResponseEntity.ok().body(books);
    }
}


// Repository    →    Service Interface   →     ServiceImpl   →    Controller  →  Testing