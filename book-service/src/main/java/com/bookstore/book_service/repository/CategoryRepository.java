package com.bookstore.book_service.repository;


import com.bookstore.book_service.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find category by slug
    Optional<Category> findBySlug(String slug);

    // Find category by name
    Optional<Category> findByNameIgnoreCase(String name);

    // Check if category exists by name
    boolean existsByName(String name);

    // Check if category exists by slug
    boolean existsBySlug(String slug);

    // Find top-level categories (no parent)
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name")
    List<Category> findTopLevelCategories();

    // Find top-level categories with pagination
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name")
    Page<Category> findTopLevelCategories(Pageable pageable);

    // Find by parent is null
    Page<Category> findByParentIsNull(Pageable pageable);

    // Find by parent is null ordered by name
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name")
    List<Category> findByParentIsNullOrderByName();

    // Find children of a category
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.name")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    // Find children of a category with pagination
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.name")
    Page<Category> findByParentId(@Param("parentId") Long parentId, Pageable pageable);

    // Find categories by name containing text
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Check if category name exists (excluding current category for updates)
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.id != :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    // Check if category slug exists (excluding current category for updates)
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.slug = :slug AND c.id != :excludeId")
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("excludeId") Long excludeId);

    // Count categories by parent
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId")
    long countByParentId(@Param("parentId") Long parentId);

    // Find categories with book count
    @Query("SELECT c, SIZE(c.books) as bookCount FROM Category c ORDER BY c.name")
    List<Object[]> findCategoriesWithBookCount();

    // Find categories that have books
    @Query("SELECT DISTINCT c FROM Category c WHERE SIZE(c.books) > 0 ORDER BY c.name")
    List<Category> findCategoriesWithBooks();

    // Find categories that have books with pagination
    @Query("SELECT DISTINCT c FROM Category c WHERE SIZE(c.books) > 0 ORDER BY c.name")
    Page<Category> findCategoriesWithBooks(Pageable pageable);
}

