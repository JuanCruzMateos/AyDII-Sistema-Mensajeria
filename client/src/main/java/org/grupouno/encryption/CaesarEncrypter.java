package org.grupouno.encryption;

/**
 * Encriptador super básico, sólo shiftea el texto una cantidad fija en base al primer caracter de cada nombre de usuario.
 */
public class CaesarEncrypter extends Encrypter {
    /**
     * Setea una clave en base a los 2 nombres de usuario de la conversación. Cada encriptador lo implementa como quiere, pero tiene que ser indistinto el orden.
     *
     * @param name1 El nombre de uno de los usuarios.
     * @param name2 El nombre del otro usuario.
     */
    @Override
    public void setKey(String name1, String name2) {
        byte keyByte = (byte) (name1.charAt(0) ^ name2.charAt(0));
        key = String.valueOf(keyByte);
    }

    /**
     * Encripta el mensaje dado por parametro y lo retorna. Hace uso de la clave seteada por setKey.
     *
     * @param data El mensaje a encriptar.
     * @return El mensaje encriptado.
     */
    @Override
    public String encrypt(String data) {
        char[] messageChars = data.toCharArray();
        byte shift = key.getBytes()[0];
        for (int i = 0; i < messageChars.length; i++) {
            messageChars[i] += shift;
        }
        return String.copyValueOf(messageChars);
    }

    /**
     * Desencripta el mensaje dado por parametro y lo retorna. Hace uso de la clave seteada por setKey.
     *
     * @param data El mensaje encriptado.
     * @return El mensaje desencriptado.
     */
    @Override
    public String decrypt(String data) {
        char[] messageChars = data.toCharArray();
        byte shift = key.getBytes()[0];
        for (int i = 0; i < messageChars.length; i++) {
            messageChars[i] -= shift;
        }
        return String.copyValueOf(messageChars);
    }
}
