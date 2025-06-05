package com.licencia.service.implement;

import com.licencia.persistence.repository.IRepositoryWinLin;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LicenciaValidacionService {

    private  final CookieService cookieService;
    private final IRepositoryWinLin winLinRepository;

    private static final String SECRET_KEY = "claveSuperSecreta1234567890123456";
    private static final String CLAVE_ENCRIPTACION = "claveAES12345678"; // exactamente 16 caracteres

    public LicenciaValidacionService(CookieService cookieService, IRepositoryWinLin winLinRepository) {
        this.cookieService = cookieService;
        this.winLinRepository = winLinRepository;
    }

    public String encriptar(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(CLAVE_ENCRIPTACION.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Generar IV (Vector de Inicialización) aleatorio
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Configurar el cifrado
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Combinar IV + datos cifrados
        byte[] encryptedIVAndText = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedIVAndText, iv.length, encrypted.length);

        // Devolver como Base64
        return Base64.getEncoder().encodeToString(encryptedIVAndText);
    }

    // En LicenciaValidacionService
    public ResponseEntity<?> validarLicencia(HttpServletResponse response, Map<String, String> body) {
        String licenciaEnc = body.get("licenciaEncriptada");

        try {
            // Verificar si la licencia encriptada existe en la base de datos
            if (!winLinRepository.existsByWiniIncrip(licenciaEnc)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Licencia no registrada");
            }

            // Desencriptar internamente para validar
            String licencia = this.desencriptar(licenciaEnc);

            if (this.isLicenciaValida(licencia)) {
                // Generar token con expiración
                String token = generarTokenAnual(licencia);

                // Obtener la fecha de expiración del token
                Date expirationDate = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getExpiration();

                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("message", "Licencia activa, acceso permitido");
                respuesta.put("token", token);
                respuesta.put("expiration", expirationDate.getTime()); // Incluir la fecha de expiración en la respuesta

                // Establecer cookie con expiración
                this.setCookieWithAnnualExpiration(response, "licenciaToken", token);

                return ResponseEntity.ok(respuesta);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Licencia expirada");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Licencia no válida");
        }
    }


     private String generarTokenAnual(String licencia) {
        // Establecer expiración para el 31 de diciembre del año actual
        LocalDate endOfYear = LocalDate.now().withMonth(12).withDayOfMonth(31);
        Date expirationDate = Date.from(endOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setSubject(licencia)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
    /*private String generarTokenAnual(String licencia) {
        // Establecer expiración a 6 minutos desde ahora
        Instant now = Instant.now();
        Instant expirationInstant = now.plus(3, ChronoUnit.MINUTES);
        Date expirationDate = Date.from(expirationInstant);

        return Jwts.builder()
                .setSubject(licencia)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }*/

   /* private void setCookieWithAnnualExpiration(HttpServletResponse response, String name, String value) {
        // Calcular expiración hasta el 31 de diciembre
        LocalDate endOfYear = LocalDate.now().withMonth(12).withDayOfMonth(31);
        long expirationInSeconds = endOfYear.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() -
                System.currentTimeMillis() / 1000;

        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(expirationInSeconds > 0 ? (int) expirationInSeconds : 0);
        cookie.setHttpOnly(true); // Recomendado para seguridad
        cookie.setPath("/");
        cookie.setSecure(true); // Solo en HTTPS
        response.addCookie(cookie);
    }*/

    private void setCookieWithAnnualExpiration(HttpServletResponse response, String name, String token) {
        // Obtener la fecha de expiración del token
        Date expirationDate = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        System.out.println("Fecha de expiración del token: " + expirationDate);
        // Calcular la expiración en segundos
        long expirationInSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;

        Cookie cookie = new Cookie(name, token);
        cookie.setMaxAge(expirationInSeconds > 0 ? (int) expirationInSeconds : 0);
        cookie.setHttpOnly(true); // Recomendado para seguridad
        cookie.setPath("/");
        cookie.setSecure(true); // Solo en HTTPS
        response.addCookie(cookie);
    }

    // Métodos existentes (encriptar, desencriptar, isLicenciaValida) se mantienen igual
    public String desencriptar(String data) throws Exception {
        byte[] encryptedIVAndText = Base64.getDecoder().decode(data);

        // Extraer IV (Vector de Inicialización)
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
    // ...

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
}
