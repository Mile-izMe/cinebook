package com.cinebook.module.user.mapper;

import com.cinebook.module.auth.dto.response.UserResponse;
import com.cinebook.module.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
