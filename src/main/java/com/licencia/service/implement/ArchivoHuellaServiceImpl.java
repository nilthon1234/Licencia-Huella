package com.licencia.service.implement;

import com.licencia.service.interfaces.IArchivoHuellaService;
import com.licencia.utils.EncriptadorHuella;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class ArchivoHuellaServiceImpl implements IArchivoHuellaService {


    private static final Logger logger = LoggerFactory.getLogger(ArchivoHuellaServiceImpl.class);


    @Override
    public String leerHuellaGuardada() {

        try {
            return EncriptadorHuella.leerHuellaDesencriptada();
        }catch (Exception e) {
            logger.error("Error....", e);
            return null;
        }
    }

    @Override
    public void guardarHuella(String fingerprint) {

        try {
            EncriptadorHuella.guardarHuellaEncriptada(fingerprint);
        }catch (Exception e){

            logger.error("Error al guardar la huella",e);
        }

    }
}
