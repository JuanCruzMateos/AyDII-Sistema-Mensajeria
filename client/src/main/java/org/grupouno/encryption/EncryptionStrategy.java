package org.grupouno.encryption;

public class EncryptionStrategy {
    Encrypter strategy;

    public EncryptionStrategy() {
        this("AES");
    }

    public EncryptionStrategy(String type) {
        setStrategy(type);
    }

    public void setStrategy(String type) {
        //No debería llegar NULL... pero por si alguien malvado lo intenta...
        if (type == null)
            type = "AES";
        switch (type) {
            case "Caesar":
                strategy = new CaesarEncrypter();
                break;
            case "AES":
            default:
                strategy = new AESEncrypter();
        }
    }

    public String encrypt(String data, String user1, String user2) {
        strategy.setKey(user1, user2);
        return strategy.encrypt(data);
    }

    public String decrypt(String data, String user1, String user2) {
        strategy.setKey(user1, user2);
        return strategy.decrypt(data);
    }

}
