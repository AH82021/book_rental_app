package com.bookstore.book_service.repository;

import com.bookstore.book_service.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository // this class will be detected automatically by spring and will be used as a bean,
public interface BookRepository extends JpaRepository<Book,Long> {

}
