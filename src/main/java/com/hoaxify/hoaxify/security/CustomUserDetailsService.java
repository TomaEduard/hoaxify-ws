package com.hoaxify.hoaxify.security;

import com.hoaxify.hoaxify.shared.response.UserPrincipal;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) throw new UsernameNotFoundException(username);

        return UserPrincipal.create(user);
    }

}
