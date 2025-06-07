package org.grupouno.encryption;

/**
 * Factory class to create instances of Encrypter based on the specified type.
 * This class uses a switch expression to determine which Encrypter implementation to instantiate.
 * <p>
 * AES is the default encrypter, while Caesar is an alternative.
 */
public class EncrypterFactory {
    // TODO: Se puede mejorar sacando el if con un factory abratracto que retorne un Encrypter y multiples factories concretos que devuelvan cada uno de los tipos de encriptadores.
    public static Encrypter createEncrypter(String type) {
        Encrypter encrypter;
        if (type.equalsIgnoreCase("Caesar")) {
            encrypter = new CaesarEncrypter();
        } else {
            // Si es AES o cualquier otro tipo, se usa el encriptador AES por defecto.
            encrypter = new AESEncrypter();
        }
        return encrypter;
    }
}
