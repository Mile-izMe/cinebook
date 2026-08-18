package com.cinebook.module.user.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.auth.dto.response.UserResponse;
import com.cinebook.module.booking.entity.BookingSnapshot;
import com.cinebook.module.booking.entity.BookingStatus;
import com.cinebook.module.booking.repository.BookingRepository;
import com.cinebook.module.user.dto.request.UpdateUserRequest;
import com.cinebook.module.user.dto.response.UserStatsResponse;
import com.cinebook.module.user.entity.User;
import com.cinebook.module.user.mapper.UserMapper;
import com.cinebook.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final UserMapper userMapper;

    public User findOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CinebookException(ErrorCode.USER_NOT_FOUND));
    }

    public UserStatsResponse getMyStats(UUID currentUserId) {
        List<Object[]> statsList = bookingRepository.getRawBookingData(currentUserId, BookingStatus.PAID);

        int totalBookings = statsList.size();
        int totalTickets = 0;
        int totalSpent = 0;

        for (Object[] row : statsList) {
            BookingSnapshot snapshot = (BookingSnapshot) row[0];
            Number price = (Number) row[1];

            if (snapshot != null && snapshot.seats() != null) {
                totalTickets += snapshot.seats().size();
            }

            totalSpent += (price != null) ? price.intValue() : 0;
        }

        return new UserStatsResponse(totalBookings, totalTickets, totalSpent);
    }

    public UserResponse updateProfile(UUID userId, UpdateUserRequest request) {
        User user = findOrThrow(userId);

        // If FE send objectKey of new avatar => Overwrite
        if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
            user.setAvatarUrl(request.avatarUrl());
        }

        if (request.userName() != null && !request.userName().isBlank()) {
            user.setUserName(request.userName());
        }

        if (request.email() != null && !request.email().isBlank()) {
            user.setPhone(request.email());
        }

        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone());
        }

        userRepository.save(user);
        return userMapper.toResponse(user);
    }
}

