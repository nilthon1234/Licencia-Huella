package com.licencia.service.interfaces;

public interface IArchivoHuellaService {

    String leerHuellaGuardada();
    void guardarHuella(String fingerprint);
}
