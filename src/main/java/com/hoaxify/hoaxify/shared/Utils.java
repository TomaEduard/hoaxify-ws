package com.hoaxify.hoaxify.shared;

import com.hoaxify.hoaxify.configuration.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class Utils {

    public String generateEmailVerificationToken(String publicUserId) {
        System.out.println("#1");

        String token = Jwts.builder()
                .setSubject(publicUserId)
                // generate a Date from today + SecurityConstants.EXPIRATION_TIME(10days) which is named Expiration
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                // use getTokenSecret() method from SecurityConstants to get the tokenSecret from application.properties
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.TOKEN_SECRET)
                .compact();
        return token;
    }

    public static boolean hasTokenExpired(String token) {
        boolean returnValue = false;
        try {
            Claims claims = Jwts.parser()
                    // use getTokenSecret() method from SecurityConstants to get the tokenSecret from application.properties
                    .setSigningKey(SecurityConstants.TOKEN_SECRET)
                    // decrypt token and save to local variable named claims
                    .parseClaimsJws(token).getBody();

            Date tokenExpirationDate = claims.getExpiration();
            Date todayDate = new Date();

            // if return true the date is expired
            returnValue = tokenExpirationDate.before(todayDate);
            System.out.println("tokenExpirationDate :" + tokenExpirationDate);
            System.out.println("todayDate :" + todayDate);
            System.out.println("returnValue :" + returnValue);
        } catch (ExpiredJwtException ex) {
            returnValue = true;
        }

        return returnValue;
    }
}
