package com.licencia.service.implement;

import com.licencia.service.interfaces.IArchivoHuellaService;
import com.licencia.utils.EncriptadorHuella;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;


@Service
public class ArchivoHuellaServiceImpl implements IArchivoHuellaService {

    private static final Logger logger = LoggerFactory.getLogger(ArchivoHuellaServiceImpl.class);
    private static final String RUTA_ARCHIVO = System.getProperty("user.home") + "/.CRIPT@/win.dat";

    @Override
    public String leerHuellaGuardada() {
        try {
            // First check if the file exists to handle first-time users
            if (!Files.exists(Paths.get(RUTA_ARCHIVO))) {
                logger.info("Archivo de huella no encontrado. Primera ejecución.");
                return null;
            }
            return EncriptadorHuella.leerHuellaDesencriptada();
        } catch (Exception e) {
            logger.error("Error al leer la huella", e);
            return null;
        }
    }

    @Override
    public void guardarHuella(String fingerprint) {
        try {
            EncriptadorHuella.guardarHuellaEncriptada(fingerprint);
            logger.info("Huella guardada exitosamente");
        } catch (Exception e) {
            logger.error("Error al guardar la huella", e);
        }
    }
}
