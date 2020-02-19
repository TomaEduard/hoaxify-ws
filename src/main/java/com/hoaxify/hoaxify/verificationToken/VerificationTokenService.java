package com.hoaxify.hoaxify.verificationToken;

import com.hoaxify.hoaxify.shared.AmazonSES;
import com.hoaxify.hoaxify.shared.Utils;
import com.hoaxify.hoaxify.shared.request.UpdateEmail;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerificationTokenService {

    VerificationTokenRepository verificationTokenRepository;

    UserRepository userRepository;

    UserService userService;

    AmazonSES amazonSES;

    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository, UserRepository userRepository, AmazonSES amazonSES) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
        this.amazonSES = amazonSES;
    }

    public void saveChangeEmailToken(VerificationToken verificationToken) {
        verificationTokenRepository.save(verificationToken);
    }

    public boolean changeEmailById(long id) {
        boolean returnValue = false;
        try {
            System.out.println("test1");
            // #1 Take the user from DB
            User userDB = userRepository.findById(id).get();
//            VerificationToken userDBVerificationToken = verificationTokenRepository.findByUser(userDB);
            System.out.println("test2");

            // verificam daca mai avem vrunu !
            VerificationToken userDBVerificationToken = verificationTokenRepository.findByUserAndChangeEmailTokenNotNull(userDB);

            // if change_email_token already exist will update it
            if (userDBVerificationToken != null) {
                System.out.println("userDBVerificationToken != null  ===> change_email_token already exist will update it ");
                userDBVerificationToken.setChangeEmailToken(new Utils().generateEmailVerificationToken(userDB.getUsername()));
                saveChangeEmailToken(userDBVerificationToken);

                amazonSES.changeEmail(userDBVerificationToken, userDB);
                returnValue = true;

            } else {
                // if change_email_token not exist will create it
                // #2 Create a VerificationToken object and add to it token + user
                System.out.println("userDBVerificationToken != null  ===> change_email_token not exist will create it ");
                VerificationToken verificationToken = new VerificationToken();
                verificationToken.setUser(userDB);
                verificationToken.setChangeEmailToken(new Utils().generateEmailVerificationToken(userDB.getUsername()));
                System.out.println("test3");

                // #4 Save to DB
                saveChangeEmailToken(verificationToken);
//                verificationTokenService.saveChangeEmailToken(verificationToken);
                System.out.println("test4");

//            userDB.setVerificationToken(verificationToken);
//            userRepository.save(userDB);

                amazonSES.changeEmail(verificationToken, userDB);
                System.out.println("test5");
                returnValue = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public boolean verifyEmailTokenChangeEmail(String token) {
        boolean returnValue = false;
        boolean hasTokenExpired = Utils.hasTokenExpired(token);

        if (!hasTokenExpired) {
            VerificationToken verificationTokenByChangeEmailToken = verificationTokenRepository.findByChangeEmailToken(token);
            verificationTokenRepository.deleteById(verificationTokenByChangeEmailToken.getId());
            returnValue = true;
        }

        return returnValue;
    }
}
