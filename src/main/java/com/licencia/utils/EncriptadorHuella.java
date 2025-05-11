package com.licencia.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class EncriptadorHuella {

    private static final String CLAVE_SECRETA = "12RMRT78DFBBC@34DSFJSWREWRE@3535";
    private  static final String RUTA_ARCHIVO = System.getProperty("user.home") + "/.CRIPT@/win.dat";

    public static  void  guardarHuellaEncriptada(String huella) throws  Exception {
        byte[] claveBytes = CLAVE_SECRETA.getBytes();
        SecretKeySpec clave = new SecretKeySpec(claveBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, clave);
        byte[] encryptedBytes = cipher.doFinal(huella.getBytes());
        Path carpeta = Paths.get(System.getProperty("user.home"), ".CRIPT@");

        if (!Files.exists(carpeta)){
            Files.createDirectories(carpeta);
            try {
                Files.setAttribute(carpeta, "dos:hidden", true);
            }catch (Exception ignored){}
        }
        Files.write(Paths.get(RUTA_ARCHIVO), Base64.getEncoder().encode(encryptedBytes));
    }

    public static String leerHuellaDesencriptada() throws Exception{
        byte[] claveBytes = CLAVE_SECRETA.getBytes();
        SecretKeySpec clave = new SecretKeySpec(claveBytes, "AES");


        byte[]  archivoBytes = Files.readAllBytes(Paths.get(RUTA_ARCHIVO));
        byte[] encryptedBytes = Base64.getDecoder().decode(archivoBytes);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, clave);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);

    }

}
