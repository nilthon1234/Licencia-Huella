package com.licencia.presentation.controller;

import com.licencia.config.LicenciaConfig;
import com.licencia.service.http.request.LicenciaRequest;
import com.licencia.service.interfaces.IArchivoHuellaService;
import com.licencia.service.interfaces.ITokenService;
import lombok.RequiredArgsConstructor;
import org.apache.el.parser.Token;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/licencia")
public class LicenciaController {

    private final LicenciaConfig config;
    private final IArchivoHuellaService archivoHuellaService;
    private final ITokenService tokenService;
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
        return ResponseEntity.ok(Map.of("token", token));
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


}
