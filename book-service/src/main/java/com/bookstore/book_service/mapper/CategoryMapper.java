package com.bookstore.book_service.mapper;

import com.bookstore.book_service.dto.CategoryCreateRequest;
import com.bookstore.book_service.dto.CategoryResponse;
import com.bookstore.book_service.dto.CategoryUpdateRequest;
import com.bookstore.book_service.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;

/**
 * MapStruct mapper for Category and its DTOs
 * MapStruct automatically generates the implementation at compile time
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {

    /**
     * Maps CategoryCreateRequest to Category entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Category toEntity(CategoryCreateRequest request);

    /**
     * Maps CategoryCreateRequest to Category entity with parent
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Category toEntity(CategoryCreateRequest request, Category parent);

    /**
     * Updates existing Category entity with CategoryUpdateRequest data
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget Category category, CategoryUpdateRequest request);

    /**
     * Updates existing Category entity with parent
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget Category category, CategoryUpdateRequest request, Category parent);

    /**
     * Converts Category entity to CategoryResponse DTO
     */
    @Mapping(target = "parentId", expression = "java(category.getParent() != null ? category.getParent().getId() : null)")
    @Mapping(target = "parentName", expression = "java(category.getParent() != null ? category.getParent().getName() : null)")
    @Mapping(target = "bookCount", expression = "java(category.getBooks() != null ? category.getBooks().size() : 0)")
    @Mapping(target = "updatedAt", ignore = true)
    CategoryResponse toResponse(Category category);

    /**
     * Converts a set of Category entities to a set of CategoryResponse DTOs
     */
    Set<CategoryResponse> toResponseSet(Set<Category> categories);
}
