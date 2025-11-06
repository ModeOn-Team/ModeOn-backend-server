package com.modeon.backend.service;

import com.modeon.backend.entity.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findUserById(Long userId);
}
