package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.configuration.SecurityConstants;
import com.hoaxify.hoaxify.error.NotFoundException;
import com.hoaxify.hoaxify.file.FileService;
//import com.hoaxify.hoaxify.shared.Utils;
import com.hoaxify.hoaxify.shared.AmazonSES;
import com.hoaxify.hoaxify.shared.Utils;
import com.hoaxify.hoaxify.user.userVM.UserUpdateVM;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class UserService {

    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    FileService fileService;

    AmazonSES amazonSES;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService, AmazonSES amazonSES) {
        super();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.amazonSES = amazonSES;
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerificationToken("TEST");
        user.setEmailVerificationToken(new Utils().generateEmailVerificationToken(user.getUsername()));
        user.setEmailVerificationStatus(false);

        amazonSES.verifyEmail(user);
        return userRepository.save(user);
    }


    public Page<User> getUser(User loggedInUser, Pageable pageable) {
        if (loggedInUser != null) {
            return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getUserByUsername(String username) {
        User inDB = userRepository.findByUsername(username);
        if (inDB == null) {
            throw new NotFoundException(username + " not found");
        }
        return inDB;
    }

    public User update(long id, UserUpdateVM userUpdateVM) {
        User inDb = userRepository.getOne(id);
        inDb.setDisplayName(userUpdateVM.getDisplayName());

        if (userUpdateVM.getImage() != null) {
            String savedImageName = null;
            try {
                savedImageName = fileService.saveProfileImage(userUpdateVM.getImage());
                // remove the old picture
                fileService.deleteProfileImage(inDb.getImage());
                inDb.setImage(savedImageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return userRepository.save(inDb);
    }

    /*
     * Email Verification
     * */
    public boolean resendEmailById(long id) {
        boolean returnValue = false;
        try {
            User userDB = userRepository.findById(id).get();
            userDB.setEmailVerificationToken(new Utils().generateEmailVerificationToken(userDB.getUsername()));
            userRepository.save(userDB);

            amazonSES.verifyEmail(userDB);
            returnValue = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    /*
    * Utils
    * */
    public User saveUserAndVerificationStatusTrueWithoutAWS(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerificationToken("TEST");
        user.setEmailVerificationToken(new Utils().generateEmailVerificationToken(user.getUsername()));
        user.setEmailVerificationStatus(true);
        user.setEmailVerificationToken(null);

        return userRepository.save(user);
    }

    public User saveUserAndVerificationStatusFalseWithoutAWS(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerificationToken("TEST");
        user.setEmailVerificationToken(new Utils().generateEmailVerificationToken(user.getUsername()));
        user.setEmailVerificationStatus(false);
        return userRepository.save(user);
    }

    public boolean verifyEmailToken(String token) {
        boolean returnValue = false;

        System.out.println("#1");
        // find user by token
        User userDB = userRepository.findUserByEmailVerificationToken(token);

        System.out.println("#2");
        // verify token expired date
        boolean hasTokenExpired = Utils.hasTokenExpired(token);
        System.out.println("#3");

        if (!hasTokenExpired) {
            userDB.setEmailVerificationToken(null);
            userDB.setEmailVerificationStatus(Boolean.TRUE);
            userRepository.save(userDB);
            returnValue = true;
            System.out.println("#4");
        }

        return returnValue;
    }
}