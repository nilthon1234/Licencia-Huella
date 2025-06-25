package com.licencia.service.implement;

import com.licencia.service.interfaces.ITokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
public class TockenServiceImpl implements ITokenService {

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor
            ("424J33K53J556N5M6M7N6K8M6K8M68M@-35353@3535".getBytes(StandardCharsets.UTF_8));
    final long EXPIRATION_TIME = 30L * 24 * 60 * 60 * 1000;


    @Override
    public String generetaToken(String fingerprint) {
        return Jwts.builder()
                .setSubject("licencia-validada")
                .claim("fingerprint", fingerprint)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME ))//1 AÑO
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean validarToken(String token, String fingerprintEsperado) {

        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build();
            Claims claims = parser.parseClaimsJws(token).getBody();
            return  fingerprintEsperado.equals(claims.get("fingerprint"));
        } catch (Exception e) {
            return false;
        }
    }
}
