package com.example.demo.Reponsitory;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}