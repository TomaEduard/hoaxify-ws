package com.hoaxify.hoaxify.io.repository;

import com.hoaxify.hoaxify.io.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

}
