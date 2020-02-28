package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.Date;
import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootApplication
public class HoaxifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoaxifyApplication.class, args);
    }

    @Bean
    @Profile("dev")
        // it only run and make 15 object in dev mode
    CommandLineRunner runDev(UserService userService) {
        return (args) -> {
            IntStream.rangeClosed(1, 4)
                    .mapToObj(i -> {
//                    UserPreference userPreference = new UserPreference();
//                    userPreference.setFavorite(false);
//                    userPreference.setLike(false);
//                    userPreference.setBookmark(false);

                        User user = new User();
                        user.setUsername("user" + i);
                        user.setDisplayName("display_user" + i);
                        user.setPassword("P4ssword");
//                        user.setUserPreference(userPreference);
                        user.setImage(UUID.randomUUID().toString().replaceAll("-", ""));
                        user.setTimestamp(new Date());
                        return user;
                    })
                    .forEach(userService::saveUserAndVerificationStatusTrueWithoutAWS);

            User user5 = new User();
            user5.setUsername("user5");
            user5.setDisplayName("display_user5");
            user5.setPassword("P4ssword");
            user5.setImage(UUID.randomUUID().toString().replaceAll("-", ""));
            user5.setTimestamp(new Date());
            userService.saveUserAndVerificationStatusFalseWithoutAWS(user5);

            User user6 = new User();
            user6.setUsername("eduard.daniel.toma@gmail.com");
            user6.setDisplayName("Eduard Toma");
            user6.setPassword("P4ssword");
            user6.setImage(UUID.randomUUID().toString().replaceAll("-", ""));
            user6.setTimestamp(new Date());
            userService.saveUserAndVerificationStatusFalseWithoutAWS(user6);

            User user7 = new User();
            user7.setUsername("admin@hoaxify.com");
            user7.setDisplayName("Admin");
            user7.setPassword("P4ssword");
            user7.setImage(UUID.randomUUID().toString().replaceAll("-", ""));
            user7.setTimestamp(new Date());
            userService.saveUserAndVerificationStatusTrueWithoutAWS(user7);

//            User user = new User();
//            user.setUsername("user1");
//            user.setDisplayName("display_user1");
//            user.setPassword("P4ssword");
//            user.setImage(UUID.randomUUID().toString().replaceAll("-", ""));
//            userService.saveUserAndVerificationStatusFalseWithoutAWS(user);
//

        };
    }

    @Bean
    @Profile("prod")
    CommandLineRunner runProd(UserService userService) {
        return (args) -> {
//            IntStream.rangeClosed(1, 4)
//                    .mapToObj(i -> {
////                    UserPreference userPreference = new UserPreference();
////                    userPreference.setFavorite(false);
////                    userPreference.setLike(false);
////                    userPreference.setBookmark(false);
//
//                        User user = new User();
//                        user.setUsername("admin" + i);
//                        user.setDisplayName("display_user" + i);
//                        user.setPassword("P4ssword");
////                        user.setUserPreference(userPreference);
//                        user.setImage(UUID.randomUUID().toString().replaceAll("-", ""));
//                        user.setTimestamp(new Date());
//                        return user;
//                    })
//                    .forEach(userService::saveUserAndVerificationStatusTrueWithoutAWS);

            if (userService.getUserByUsernameForCommandLineRunner("admin@hoaxify.com") == null) {
                User user5 = new User();
                user5.setUsername("admin@hoaxify.com");
                user5.setDisplayName("Admin");
                user5.setPassword("P4ssword");
                user5.setImage(UUID.randomUUID().toString().replaceAll("-", ""));
                user5.setTimestamp(new Date());
                userService.saveUserAndVerificationStatusTrueWithoutAWS(user5);
            }

//            User user = new User();
//            user.setUsername("user1");
//            user.setDisplayName("display_user1");
//            user.setPassword("P4ssword");
//            user.setImage(UUID.randomUUID().toString().replaceAll("-", ""));
//            userService.saveUserAndVerificationStatusFalseWithoutAWS(user);

        };
    }
}
