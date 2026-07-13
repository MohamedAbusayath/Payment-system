package com.payment.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class ServiceJwtProvider {
    private final SecretKey key;
    private final long exp;

   public ServiceJwtProvider(@Value("${service.jwt.secret}")String secret,
                       @Value("${service.jwt.expiration}")long exp){
        this.key= Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.exp=exp;
    }

    public String generateToken(){
        Date now = new Date();
        return Jwts.builder()
                .subject("payment-service")
                .claim("role", "SERVICE")
                .issuedAt(now)
                .expiration(new Date(now.getTime()+exp))
                .signWith(key)
                .compact();
    }


}
