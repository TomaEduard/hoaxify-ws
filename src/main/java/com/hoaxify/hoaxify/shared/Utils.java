package com.hoaxify.hoaxify.shared;

import com.hoaxify.hoaxify.configuration.SecurityConstants;
import com.hoaxify.hoaxify.shared.response.UserPrincipal;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SecurityConstants.TOKEN_SECRET)
                .parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserPrincipal userPrincipal) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userPrincipal.getUsername(), userPrincipal.getId());
    }

    private String createToken(Map<String, Object> claims, String subject, long userId) {

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(String.valueOf(userId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SecurityConstants.TOKEN_SECRET)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateEmailVerificationToken(String publicUserId) {
        logger.debug("Utils.generateEmailVerificationToken(String publicUserId)", publicUserId);

        String token = Jwts.builder()
                .setSubject(publicUserId)
                .setExpiration(new Date(System.currentTimeMillis()+ SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.TOKEN_SECRET)
                .compact();
        return token;
    }

    public static boolean hasTokenExpired(String token) {
        boolean returnValue = false;
        try {
            Claims claims = Jwts.parser()
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

        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }

        return returnValue;
    }
}
