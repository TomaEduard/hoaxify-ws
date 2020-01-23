package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootApplication
public class HoaxifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoaxifyApplication.class, args);
    }

    @Bean
    @Profile("!test")
    CommandLineRunner run(UserService userService) {
        return (args) -> {
            IntStream.rangeClosed(1, 15)
                    .mapToObj(i -> {
                        User user = new User();
                        user.setUsername("user" + i);
                        user.setDisplayName("display" + i);
                        user.setPassword("P4ssword");
                        user.setImage(UUID.randomUUID().toString().replaceAll("-", ""));
                        return user;
                    })
                    .forEach(userService::save);
        };
    }
}
