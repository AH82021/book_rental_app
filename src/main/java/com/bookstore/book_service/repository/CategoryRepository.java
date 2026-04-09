package com.bookstore.book_service.repository;

import com.bookstore.book_service.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
   // History, Fiction , Non-Fiction, Science, Technology, Biography, Children, Mystery, Romance, Fantasy
   Optional<Category> findByNameIgnoreCase(String name);//  Non-Fiction ,  non-fiction


    Optional<Category> findBySlug(String slug);
     boolean existsByName(String name);

     boolean existsBySlug(String slug);

}
