package com.licencia.service.http.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LicenciaRequest {

    private String licencia;


    private String fingerprint;
}
