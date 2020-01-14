package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
