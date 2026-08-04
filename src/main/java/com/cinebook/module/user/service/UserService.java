package com.cinebook.module.user.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.user.entity.User;
import com.cinebook.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CinebookException(ErrorCode.USER_NOT_FOUND));
    }
}
