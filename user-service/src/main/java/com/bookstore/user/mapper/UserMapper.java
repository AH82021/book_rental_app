package com.bookstore.user.mapper;

import com.bookstore.user.dto.UserProfileResponse;
import com.bookstore.user.dto.UserProfileUpdateRequest;
import com.bookstore.user.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    /**
     * Map UserProfile entity to UserProfileResponse DTO
     */
    UserProfileResponse toResponse(UserProfile userProfile);

    /**
     * Map UserProfileUpdateRequest DTO to UserProfile entity
     */
    UserProfile toEntity(UserProfileUpdateRequest updateRequest);

    /**
     * Update UserProfile entity with data from UserProfileUpdateRequest DTO
     */
    void updateEntityFromRequest(UserProfileUpdateRequest updateRequest, @MappingTarget UserProfile userProfile);
}

