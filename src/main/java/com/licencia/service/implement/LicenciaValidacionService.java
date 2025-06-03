package com.licencia.service.implement;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Service
public class LicenciaValidacionService {

    private static final String SECRET_KEY = "claveSuperSecreta1234567890123456";
    private static final String CLAVE_ENCRIPTACION = "claveAES12345678"; // exactamente 16 caracteres

    // En LicenciaValidacionService
    public boolean validarLicencia(String jwt) {
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
}
