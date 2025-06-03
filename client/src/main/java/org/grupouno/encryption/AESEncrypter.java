package org.grupouno.encryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Implementación de la famosísima encriptación AES con clave en base a los hashes de los nombres de usuario.
 */
public class AESEncrypter extends Encrypter {

    /**
     * Setea una clave en base a los 2 nombres de usuario de la conversación. Cada encriptador lo implementa como quiere, pero tiene que ser indistinto el orden.
     *
     * @param name1 El nombre de uno de los usuarios.
     * @param name2 El nombre del otro usuario.
     */
    @Override
    public void setKey(String name1, String name2) {
        this.key = Integer.toHexString(name1.hashCode() + name2.hashCode());
    }

    /**
     * Encripta el mensaje dado por parametro y lo retorna. Hace uso de la clave seteada por setKey.
     *
     * @param data El mensaje a encriptar.
     * @return El mensaje encriptado.
     */
    @Override
    public String encrypt(String data) {
        try {

            // Create default byte array
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec
                    = new IvParameterSpec(iv);

            // Create SecretKeyFactory object
            SecretKeyFactory factory
                    = SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256");

            // Create KeySpec object and assign with
            // constructor
            KeySpec spec = new PBEKeySpec(
                    key.toCharArray(), "SALT".getBytes(),
                    65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(
                    tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance(
                    "AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,
                    ivspec);
            // Return encrypted string
            return Base64.getEncoder().encodeToString(
                    cipher.doFinal(data.getBytes(
                            StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: "
                    + e);
        }
        return null;
    }

    /**
     * Desencripta el mensaje dado por parametro y lo retorna. Hace uso de la clave seteada por setKey.
     *
     * @param data El mensaje encriptado.
     * @return El mensaje desencriptado.
     */
    @Override
    public String decrypt(String data) {
        try {

            // Default byte array
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0};
            // Create IvParameterSpec object and assign with
            // constructor
            IvParameterSpec ivspec
                    = new IvParameterSpec(iv);

            // Create SecretKeyFactory Object
            SecretKeyFactory factory
                    = SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256");

            // Create KeySpec object and assign with
            // constructor
            KeySpec spec = new PBEKeySpec(
                    key.toCharArray(), "SALT".getBytes(),
                    65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(
                    tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance(
                    "AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey,
                    ivspec);
            // Return decrypted string
            return new String(cipher.doFinal(
                    Base64.getDecoder().decode(data)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: "
                    + e);
        }
        return null;
    }
}
