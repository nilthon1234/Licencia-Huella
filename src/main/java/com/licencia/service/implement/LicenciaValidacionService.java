package com.licencia.service.implement;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LicenciaValidacionService {

    private  final CookieService cookieService;

    private static final String SECRET_KEY = "claveSuperSecreta1234567890123456";
    private static final String CLAVE_ENCRIPTACION = "claveAES12345678"; // exactamente 16 caracteres

    public LicenciaValidacionService(CookieService cookieService) {
        this.cookieService = cookieService;
    }

    // En LicenciaValidacionService
    public ResponseEntity<?> validarLicencia(HttpServletResponse response, Map<String, String> body) {
        String licenciaEnc = body.get("licenciaEncriptada");

        try {
            String licencia = this.desencriptar(licenciaEnc);
            if (this.isLicenciaValida(licencia)) {
                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("message", "licencia activa, acceso permitido");
                respuesta.put("token", licencia);

                // Establecer cookie
                this.setCookieWithLicenciaExpiration(response, "licenciaToken", licencia, getExpirationDate(licencia));

                return ResponseEntity.ok(respuesta);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Licencia expirada");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Licencia no válida");
        }
    }


    public String encriptar(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(CLAVE_ENCRIPTACION.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Generar IV aleatorio
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());

        // Concatenar IV + datos cifrados para poder desencriptar después
        byte[] encryptedIVAndText = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedIVAndText, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedIVAndText);
    }

    public String desencriptar(String data) throws Exception {
        byte[] encryptedIVAndText = Base64.getDecoder().decode(data);

        // Extraer IV
        byte[] iv = new byte[16];
        System.arraycopy(encryptedIVAndText, 0, iv, 0, iv.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Extraer datos cifrados
        int encryptedSize = encryptedIVAndText.length - iv.length;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIVAndText, iv.length, encryptedBytes, 0, encryptedSize);

        SecretKeySpec secretKey = new SecretKeySpec(CLAVE_ENCRIPTACION.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] original = cipher.doFinal(encryptedBytes);
        return new String(original);
    }

    public boolean isLicenciaValida(String jwt) {
        try {
            Jws<Claims> jwsClaims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(jwt);

            Claims claims = jwsClaims.getBody();
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            System.err.println("Error validando licencia: " + e.getMessage());
            return false;
        }
    }


    private Date getExpirationDate(String jwt) {
        // Extraer y devolver la fecha de expiración del JWT
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getExpiration();
    }

    private void setCookieWithLicenciaExpiration(HttpServletResponse response, String name, String value, Date licenciaExpiration) {
        long expirationInSeconds = TimeUnit.MILLISECONDS.toSeconds(licenciaExpiration.getTime() - System.currentTimeMillis());

        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(expirationInSeconds > 0 ? (int) expirationInSeconds : 0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
