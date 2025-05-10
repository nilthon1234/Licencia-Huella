package com.licencia.service.http.request;

import lombok.Data;

@Data
public class LicenciaRequest {
    private String licencia;
    private String fingerprint;
}
