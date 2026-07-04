package com.clinica.notificaciones.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

// Cifrado simétrico AES/GCM para el password SMTP guardado en BD.
// La clave (32 bytes, base64) vive fuera del repositorio en SMTP_CONFIG_SECRET.
@Component
public class CryptoUtil {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int TAMANO_IV = 12;
    private static final int TAMANO_TAG_BITS = 128;

    private final SecretKeySpec clave;

    public CryptoUtil(@Value("${notificaciones.smtp-config-secret}") String secretoBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(secretoBase64);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "notificaciones.smtp-config-secret debe decodificar a 32 bytes (AES-256). " +
                    "Genere uno con: openssl rand -base64 32");
        }
        this.clave = new SecretKeySpec(keyBytes, "AES");
    }

    public String cifrar(String textoPlano) {
        try {
            byte[] iv = new byte[TAMANO_IV];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(TAMANO_TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            byte[] resultado = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(cifrado, 0, resultado, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al cifrar el password SMTP", ex);
        }
    }

    public String descifrar(String textoCifradoBase64) {
        try {
            byte[] datos = Base64.getDecoder().decode(textoCifradoBase64);
            byte[] iv = new byte[TAMANO_IV];
            System.arraycopy(datos, 0, iv, 0, TAMANO_IV);
            byte[] cifrado = new byte[datos.length - TAMANO_IV];
            System.arraycopy(datos, TAMANO_IV, cifrado, 0, cifrado.length);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(TAMANO_TAG_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al descifrar el password SMTP", ex);
        }
    }
}
