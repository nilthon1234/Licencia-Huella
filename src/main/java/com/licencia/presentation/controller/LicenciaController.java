package com.licencia.presentation.controller;

import com.licencia.config.LicenciaConfig;
import com.licencia.persistence.models.WinLin;
import com.licencia.persistence.repository.IRepositoryWinLin;
import com.licencia.service.http.request.LicenciaRequest;
import com.licencia.service.implement.LicenciaValidacionService;
import com.licencia.service.interfaces.IArchivoHuellaService;
import com.licencia.service.interfaces.ITokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/licencia")
public class LicenciaController {

    private final LicenciaConfig config;
    private final IArchivoHuellaService archivoHuellaService;
    private final ITokenService tokenService;

    private final LicenciaValidacionService servicio;
    private final IRepositoryWinLin repo;
    final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 4 minutos

    @PostMapping("/validar")
    public ResponseEntity<?> validarLicencia(@RequestBody LicenciaRequest request){
        String licenciaIngresada = request.getLicencia();
        String fingerprint = request.getFingerprint();

        if (!licenciaIngresada.equals(config.getClaveEncriptada())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Licencia invalida");
        }
        String huellaGuardada = archivoHuellaService.leerHuellaGuardada();
        if (huellaGuardada == null){
            archivoHuellaService.guardarHuella(fingerprint);
        } else if (!huellaGuardada.equals(fingerprint)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Licencia ya usada en otra Pc");
        }
        String token = tokenService.generetaToken(fingerprint);
        Date expiracion = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
        return ResponseEntity.ok(Map.of("token", token, "expiration", expiracion ));
    }

    @PostMapping("/verificar")
    public ResponseEntity<?> verificarToken(@RequestBody Map<String, String> request){
        String token = request.get("token");
        String fingerprint = request.get("fingerprint");
        if (tokenService.validarToken(token, fingerprint)){
            return  ResponseEntity.ok("Token Valido");
        }else {
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalido");
        }
    }

    @PostMapping("/verificar-licencia")
    public ResponseEntity<?> verificar(@RequestBody Map<String, String> body) {
        String licencia = body.get("licencia");

        if (!servicio.isLicenciaValida(licencia)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Licencia inválida o expirada");
        }

        try {
            String licenciaEnc = servicio.encriptar(licencia);
            WinLin registro = new WinLin();
            registro.setWiniIncrip(licenciaEnc);
            registro.setDateRegister(new Date());
            repo.save(registro);
            return ResponseEntity.ok("Licencia válida y registrada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al encriptar licencia");
        }
    }

    @PostMapping("/validar-licencia")
    public ResponseEntity<?> validar(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String licenciaEnc = body.get("licenciaEncriptada");

        try {
            String licencia = servicio.desencriptar(licenciaEnc);
            // Llama al método del servicio que maneja la lógica de validación y cookies
            return servicio.validarLicencia(response, body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Licencia no válida");
        }
    }


}
