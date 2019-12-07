package com.hoaxify.hoaxify.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user) {

       return userRepository.save(user);
    }
}
