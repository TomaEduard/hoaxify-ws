package com.hoaxify.hoaxify.shared;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.hoaxify.hoaxify.configuration.SecurityConstants;
import com.hoaxify.hoaxify.user.User;
import org.springframework.stereotype.Service;

@Service
public class AmazonSES {
    // This address must be verified with Amazon SES.
    final String FROM = "tester.application.email@gmail.com";

    // The subject line for the email.
    final String SUBJECT = "One last step to complete your registration with Hoaxify App";

    final String PASSWORD_RESET_SUBJECT = "Password reset request";

    // verification email messages
    // The HTML body for the email.
    final String HTMLBODY = "<h1>Please verify your email address</h1>"
            + "<p>Thank you for registering with our Hoaxify App. To complete registration process and be able to log in,"
            + " click on the following link: "
//            + "<a href='http://ec2-52-59-187-214.eu-central-1.compute.amazonaws.com:8080/verification-service/email-verification.html?token=$tokenValue'>"
            + "<a href='http://localhost:3000/#/verification/confirmationToken?token=$tokenValue'>"
            + "Final step to complete your registration" + "</a><br/><br/>"
            + "Thank you! And we are waiting for you inside!";

    // The email body for recipients with non-HTML email clients.
    final String TEXTBODY = "Please verify your email address. "
            + "Thank you for registering with our MHoaxify app. To complete registration process and be able to log in,"
            + " open then the following URL in your browser window: "
//            + " http://ec2-52-59-187-214.eu-central-1.compute.amazonaws.com:8080/verification-service/email-verification.html?token=$tokenValue"
            + "<a href='http://localhost:3000/#/verification/confirmationToken?token=$tokenValue'>"
            + " Thank you! And we are waiting for you inside!";

    // password reset messages
    final String PASSWORD_RESET_HTMLBODY = "<h1>A request to reset your password</h1>"
            + "<p>Hi, $firstName!</p> "
            + "<p>Someone has requested to reset your password with our project. If it were not you, please ignore it."
            + " otherwise please click on the link below to set a new password: "
            + "<a href='http://localhost:8088/verification_service_war/password-reset.html?token=$tokenValue'>"
            + " Click this link to Reset Password"
            + "</a><br/><br/>"
            + "Thank you!";

    // The email body for recipients with non-HTML email clients.
    final String PASSWORD_RESET_TEXTBODY = "A request to reset your password "
            + "Hi, $firstName! "
            + "Someone has requested to reset your password with our project. If it were not you, please ignore it."
            + " otherwise please open the link below in your browser window to set a new password: "
            + " http://localhost:8088/verification_service_war/password-reset.html?token=$tokenValue"
            + " Thank you!";


    public void verifyEmail(User user) {

        System.setProperty("aws.accessKeyId", SecurityConstants.ACCESS_KEY_ID);
        System.setProperty("aws.secretKey", SecurityConstants.SECRET_KEY);

        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();

        String htmlBodyWithToken = HTMLBODY.replace("$tokenValue", user.getEmailVerificationToken());
        String textBodyWithToken = TEXTBODY.replace("$tokenValue", user.getEmailVerificationToken());

        SendEmailRequest request = new SendEmailRequest()
                // set destination
                .withDestination(new Destination().withToAddresses(user.getUsername()))
                // set messages
                .withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBodyWithToken))
                                .withText(new Content().withCharset("UTF-8").withData(textBodyWithToken)))
                        .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
                // set from
                .withSource(FROM);

        client.sendEmail(request);

        System.out.println("Email sent to: " + user.getUsername());

    }

    public boolean sendPasswordResetRequest(String firstName, String email, String token) {

        System.setProperty("aws.accessKeyId", SecurityConstants.ACCESS_KEY_ID);
        System.setProperty("aws.secretKey", SecurityConstants.SECRET_KEY);

        boolean returnValue = false;

        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();

        String htmlBodyWithToken = PASSWORD_RESET_HTMLBODY.replace("$tokenValue", token);
        htmlBodyWithToken = htmlBodyWithToken.replace("$firstName", firstName);

        String textBodyWithToken = PASSWORD_RESET_TEXTBODY.replace("$tokenValue", token);
        textBodyWithToken = textBodyWithToken.replace("$firstName", firstName);

        SendEmailRequest request = new SendEmailRequest()
                // set destination
                .withDestination(new Destination().withToAddresses( email ) )
                // set messages
                .withMessage(new Message()
                        .withBody(new Body()
                                .withHtml(new Content()
                                        .withCharset("UTF-8").withData(htmlBodyWithToken))
                                .withText(new Content()
                                        .withCharset("UTF-8").withData(textBodyWithToken)))
                        .withSubject(new Content()
                                .withCharset("UTF-8").withData(PASSWORD_RESET_SUBJECT)))
                // set from
                .withSource(FROM);

        SendEmailResult result = client.sendEmail(request);
        if(result != null && (result.getMessageId()!=null && !result.getMessageId().isEmpty())) {
            returnValue = true;
        }

        System.out.println("Email sent to: " + email);
        return returnValue;
    }
}
