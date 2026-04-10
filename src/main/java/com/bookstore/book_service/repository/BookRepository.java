package com.bookstore.book_service.repository;

import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository // this class will be detected automatically by spring and will be used as a bean,
public interface BookRepository extends JpaRepository<Book,Long> {


    Page<Book> findByDeletedFalse(Pageable pageable);
  // page 0 10 books
  // page 1  10 books
  // page 2  10 books
     Optional<Book> findByIdAndDeletedFalse(Long id);


    @Query("SELECT DISTINCT b FROM Book b WHERE b.deleted = false AND " +
            "(:keyword IS NULL OR " +
            " LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book>  searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

      /// to do an advance search

    @Query("""
       SELECT DISTINCT b FROM Book b LEFT JOIN b.categories c WHERE b.deleted = false AND
       (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%'))) AND
       (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', CAST(:author AS string), '%'))) AND
       (:isbn IS NULL OR b.isbn = :isbn) AND
       (:categoryId IS NULL OR c.id = :categoryId) AND
       (:status IS NULL OR b.status = :status) AND
       (:minPrice IS NULL OR b.price >= :minPrice) AND
       (:maxPrice IS NULL OR b.price <= :maxPrice) AND
       (:publisher IS NULL OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', CAST(:publisher AS string), '%'))) AND
       (:language IS NULL OR LOWER(b.language) = LOWER(CAST(:language AS string))) AND
       (:available IS NULL OR
              (:available = true AND b.status = 'AVAILABLE' AND b.availableCopies > 0) OR
              (:available = false AND (b.status != 'AVAILABLE' OR b.availableCopies = 0)))
       """)
    Page<Book> searchBooks(@Param("title") String title,
                           @Param("author") String author,
                           @Param("isbn") String isbn,
                           @Param("categoryId") Long categoryId,
                           @Param("status") BookStatus status,
                           @Param("minPrice") BigDecimal minPrice,
                           @Param("maxPrice") BigDecimal maxPrice,
                           @Param("publisher") String publisher,
                           @Param("language") String language,
                           @Param("available") Boolean available,
                           Pageable pageable);


}
