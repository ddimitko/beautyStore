package com.ddimitko.beautyshopproject.configs.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    public static final String SECRET = "43eeca2a05cf521dea8148cd6cf3f6e5d801bf158c73eb5b7f9a23ccb5826fcc91da1348f6cd818dad55975eaf39254676574af23618304d030ed5b2668e85fe681f33be5e3b3bad05fd74d336f0fb7cc29f66646ee032635c673d08dc41d293549726af3f1b17f191dd70ffb69fdc579d379d88dbdbf9ce41d1ccd92dcfc30f8ad89a6b3e63099fcee27bbd86510d2f57dc512985c238ac747c281637d396059ab8af00791c62ad6788bec3f188ff10d70008591f72e30e56c2d9a3edd667d1896830b0f60ae54fb8856eb8bc28c4b08247bbfc5c3413ddf008564ed6378041d5baa7e967557890c77db6ca7c16f58fb6f07be055cd21638173f8098aee75f1";

    private SecretKey getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }


    public String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder().claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minutes
                .signWith(getSigningKey(), Jwts.SIG.HS256).compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 days
                .signWith(getSigningKey(), Jwts.SIG.HS256).compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


}
