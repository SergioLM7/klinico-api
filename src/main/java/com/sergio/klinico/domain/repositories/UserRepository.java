package com.sergio.klinico.domain.repositories;

import com.sergio.klinico.domain.models.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
}