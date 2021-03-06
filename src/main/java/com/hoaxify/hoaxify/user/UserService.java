package com.hoaxify.hoaxify.user;

import com.hoaxify.hoaxify.error.NotFoundException;
import com.hoaxify.hoaxify.file.FileService;
import com.hoaxify.hoaxify.shared.AmazonSES;
import com.hoaxify.hoaxify.shared.Utils;
import com.hoaxify.hoaxify.shared.request.LoginRequestOAuth2;
import com.hoaxify.hoaxify.shared.request.UpdateEmail;
import com.hoaxify.hoaxify.user.userVM.UserUpdateVM;
import com.hoaxify.hoaxify.verificationToken.VerificationToken;
import com.hoaxify.hoaxify.verificationToken.VerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

//import com.hoaxify.hoaxify.shared.Utils;

@Service
public class UserService {

    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    FileService fileService;

    AmazonSES amazonSES;

    VerificationTokenService verificationTokenService;

    Utils utils;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService, AmazonSES amazonSES, VerificationTokenService verificationTokenService, Utils utils) {
        super();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.amazonSES = amazonSES;
        this.verificationTokenService = verificationTokenService;
        this.utils = utils;
    }

    public User save(User user) {
        try {
            user.setProvider(AuthProvider.local);
            user.setTimestamp(new Date());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setEmailVerificationToken(utils.generateEmailVerificationToken(user.getUsername()));
            user.setEmailVerificationStatus(true);
//            amazonSES.verifyEmail(user);
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("User save: ", e);
        }
        return null;
    }

    public User saveOAuth2(LoginRequestOAuth2 loginRequestOAuth2, AuthProvider authProvider) {
        try {
            User user = new User();
            user.setUsername(loginRequestOAuth2.getUsername());
            user.setDisplayName(loginRequestOAuth2.getDisplayName());
            user.setImage(loginRequestOAuth2.getImage());
            user.setProvider(authProvider);
            user.setTimestamp(new Date());
//            user.setPassword(passwordEncoder.encode(user.getPassword()));
//            user.setEmailVerificationToken(utils.generateEmailVerificationToken(user.getUsername()));
            user.setEmailVerificationStatus(true);
//            amazonSES.verifyEmail(user);
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("User save: ", e);
        }
        return null;
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

    public boolean getUserByUsernameReturnBoolean(String username) {
        return userRepository.existsByUsername(username);
    }

    public User getUserByUsernameForCommandLineRunner(String username) {
        User inDB = userRepository.findByUsername(username);
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

    public boolean changeEmailById(long id) {
        boolean returnValue = false;
        try {
            // #1 iei userul cu id'ul din baza
            User userDB = userRepository.findById(id).get();

            // #2 creezi un obiect VerificationToken si ii adaugi userul si tokenul
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setUser(userDB);
            verificationToken.setChangeEmailToken(new Utils().generateEmailVerificationToken(userDB.getUsername()));

            // #4 salvezi oebiectul in baza
            verificationTokenService.saveChangeEmailToken(verificationToken);

//            userDB.setVerificationToken(verificationToken);
//            userRepository.save(userDB);

            // #5 trimiti email cu tokenul
            amazonSES.changeEmail(verificationToken, userDB);

            returnValue = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public boolean changeEmail(User loggedInUser, UpdateEmail updateEmail) {
        boolean returnValue = false;
        try {
//            User inDb = userRepository.getOne(id);
            User inDb = userRepository.findByUsername(loggedInUser.getUsername());
            inDb.setUsername(updateEmail.getNewEmail());
            userRepository.save(inDb);
            returnValue = true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return returnValue;
    }

//    public User update(long id, UserUpdateVM userUpdateVM) {
//        User inDb = userRepository.getOne(id);
//        inDb.setDisplayName(userUpdateVM.getDisplayName());
//
//        if (userUpdateVM.getImage() != null) {
//            String savedImageName = null;
//            try {
//                savedImageName = fileService.saveProfileImage(userUpdateVM.getImage());
//                // remove the old picture
//                fileService.deleteProfileImage(inDb.getImage());
//                inDb.setImage(savedImageName);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return userRepository.save(inDb);
//    }

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
        // find user by token
        User userDB = userRepository.findUserByEmailVerificationToken(token);
        // verify token expired date
        boolean hasTokenExpired = Utils.hasTokenExpired(token);

        if (!hasTokenExpired) {
            userDB.setEmailVerificationToken(null);
            userDB.setEmailVerificationStatus(Boolean.TRUE);
            userRepository.save(userDB);
            returnValue = true;
        }

        return returnValue;
    }
}