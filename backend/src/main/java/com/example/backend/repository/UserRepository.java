package com.example.backend.repository;

import com.example.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

/**
 * Repository for User CRUD operations.
 */
public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
