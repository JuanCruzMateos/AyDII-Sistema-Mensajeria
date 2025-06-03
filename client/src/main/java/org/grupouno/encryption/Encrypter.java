package org.grupouno.encryption;

public abstract class Encrypter {
    protected String key = "";

    /**
     * Setea una clave en base a los 2 nombres de usuario de la conversación. Cada encriptador lo implementa como quiere, pero tiene que ser indistinto el orden.
     *
     * @param name1 El nombre de uno de los usuarios.
     * @param name2 El nombre del otro usuario.
     */
    public abstract void setKey(String name1, String name2);

    /**
     * Encripta el mensaje dado por parametro y lo retorna. Hace uso de la clave seteada por setKey.
     *
     * @param data El mensaje a encriptar.
     * @return El mensaje encriptado.
     */
    public abstract String encrypt(String data);

    /**
     * Desencripta el mensaje dado por parametro y lo retorna. Hace uso de la clave seteada por setKey.
     *
     * @param data El mensaje encriptado.
     * @return El mensaje desencriptado.
     */
    public abstract String decrypt(String data);

}
