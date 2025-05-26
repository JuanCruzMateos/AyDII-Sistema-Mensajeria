package org.grupouno.encryption;

public abstract class Encrypter {
    protected String key = "";

    public void setKey(String name1, String name2) {
        this.key = Integer.toHexString(name1.hashCode() + name2.hashCode());
    }

    public abstract String encrypt(String data);

    public abstract String decrypt(String data);

}
