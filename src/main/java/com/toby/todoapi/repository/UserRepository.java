package com.toby.todoapi.repository;

import com.toby.todoapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //email -> user -> email 已存在？
    Optional<User> findByEmail(String email);
    //email 已存在？
    boolean existsByEmail(String email);
}