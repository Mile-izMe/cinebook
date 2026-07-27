package com.cinebook.module.review.mapper;

import com.cinebook.module.review.dto.response.ReviewResponse;
import com.cinebook.module.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// componentModel = "spring": MapStruct auto mark generated class is @Component
// @Autowired / Inject into Service.
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.userName", target = "userName")
    @Mapping(source = "user.avatarUrl", target = "userAvatarUrl")
    ReviewResponse toResponse(Review review);
}
