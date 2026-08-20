package com.cinebook.module.user.mapper;

import com.cinebook.module.auth.dto.response.UserResponse;
import com.cinebook.module.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roleCode", source = "user.role.roleCode")
    UserResponse toResponse(User user);
}
