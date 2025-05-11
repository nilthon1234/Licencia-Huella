package com.licencia.service.interfaces;

public interface ITokenService {

    String generetaToken(String fingerprint);
    boolean validarToken(String token, String fingerprintEsperado);
}
