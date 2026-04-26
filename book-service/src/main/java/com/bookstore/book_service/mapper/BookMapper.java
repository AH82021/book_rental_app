package com.bookstore.book_service.mapper;

import com.bookstore.book_service.dto.BookCreateRequest;
import com.bookstore.book_service.dto.BookResponse;
import com.bookstore.book_service.dto.BookUpdateRequest;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.model.Book;
import com.bookstore.book_service.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;

/**
 * MapStruct mapper for Book, Category, and their DTOs
 * MapStruct automatically generates the implementation at compile time
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BookMapper {

    /**
     * Maps BookCreateRequest to Book entity
     * Custom mappings handle default values
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "language", source = "language", defaultValue = "English")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)

    @Mapping(target = "availableCopies", source = "availableCopies", defaultValue = "0")
    Book toEntity(BookCreateRequest request);

    /**
     * Maps BookCreateRequest to Book entity with categories
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "language", source = "request.language", defaultValue = "English")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Book toEntity(BookCreateRequest request, Set<Category> categories);

    /**
     * Updates existing Book entity with BookUpdateRequest data
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isbn", ignore = true)
    @Mapping(target = "language", source = "language", defaultValue = "English")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(@MappingTarget Book book, BookUpdateRequest request);

    /**
     * Converts Book entity to BookResponse DTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "isbn", source = "isbn")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "rentalPrice", source = "rentalPrice")
    @Mapping(target = "publicationDate", source = "publicationDate")
    @Mapping(target = "publisher", source = "publisher")
    @Mapping(target = "pages", source = "pages")
    @Mapping(target = "language", source = "language")
    @Mapping(target = "coverImageUrl", source = "coverImageUrl")
    @Mapping(target = "totalCopies", source = "totalCopies")
    @Mapping(target = "availableCopies", source = "availableCopies")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "categories", source = "categories")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "isAvailable", expression = "java(book.getAvailableCopies() != null && book.getAvailableCopies() > 0)")
    BookResponse toResponse(Book book);

    /**
     * Converts a set of Book entities to a set of BookResponse DTOs
     */
    Set<BookResponse> toResponseSet(Set<Book> books);

    /**
     * Converts Category entity to CategoryResponse DTO
     */
    @Mapping(target = "parentId", expression = "java(category.getParent() != null ? category.getParent().getId() : null)")
    @Mapping(target = "parentName", expression = "java(category.getParent() != null ? category.getParent().getName() : null)")
    @Mapping(target = "bookCount", expression = "java(category.getBooks() != null ? category.getBooks().size() : 0)")
    @Mapping(target = "updatedAt", ignore = true)
    CategoryResponse toCategoryResponse(Category category);

    /**
     * Converts a set of Category entities to a set of CategoryResponse DTOs
     */
    Set<CategoryResponse> toCategoryResponseSet(Set<Category> categories);


}
