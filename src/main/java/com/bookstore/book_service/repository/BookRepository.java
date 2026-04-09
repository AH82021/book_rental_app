package com.bookstore.book_service.repository;

import com.bookstore.book_service.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

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
}
