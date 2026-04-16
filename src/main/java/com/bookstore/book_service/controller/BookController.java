package com.bookstore.book_service.controller;


import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.BookUpdateRequest;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import com.bookstore.book_service.service.BookService;
import com.bookstore.book_service.service.impl.BookServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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



@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/books")

public class BookController {


    private final BookService bookService  ;



    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(@PageableDefault(size = 20, sort ="title") Pageable  pageable) {

        Page<BookResponse> books = bookService.findAllBooks(pageable);
        return  ResponseEntity.ok().body(books);
    }

    @GetMapping ("/status/{status}")
    @Operation(summary = "Get book by status", description = "Retrieves books with specific status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Book retrieved Successfully"),

    })

    public ResponseEntity<Page<BookResponse>> getBooksByStatus  ( @Parameter(description = "Book Status") @PathVariable BookStatus status, @PageableDefault(size = 20, sort= "title") Pageable pageable) {
      Page<BookResponse> books = bookService.getBooksByStatus(status, pageable);
      return  ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Returns a single book by its ID. The book must not be marked as deleted.")
    public ResponseEntity<BookResponse> getBookById(
            @Parameter(description = "ID of the book to retrieve", example = "1")
            @PathVariable Long id){

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
    @DeleteMapping("/{Id}")
    @Operation(summary = "Delete book by ID", description = "Soft Delete a book by marking it as deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",description = "Book deleted Successfully"),
            @ApiResponse(responseCode = "404",description = "Book Not Found")
    })
    public ResponseEntity<Void> deleteBookById(@Parameter(description = "Book deleted by ID", example = "1")
                                                   @PathVariable Long Id){
        log.info("Deleting book by ID:{}", Id);
        bookService.deleteBookById(Id);
        return ResponseEntity.noContent().build();


    }


    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Updates an existing book with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        log.info("Updating book with ID: {}", id);
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }
}


