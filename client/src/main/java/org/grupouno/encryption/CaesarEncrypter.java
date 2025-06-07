package org.grupouno.encryption;

public class CaesarEncrypter extends Encrypter {
    @Override
    public void setKey(String name1, String name2) {
        this.key = String.valueOf((byte) (name1.charAt(0) ^ name2.charAt(0)));
    }

    @Override
    public String encrypt(String data) {
        return this.shiftChars(data, true);
    }

    @Override
    public String decrypt(String data) {
        return this.shiftChars(data, false);
    }

    private String shiftChars(String data, boolean encrypt) {
        char[] chars = data.toCharArray();
        byte shift = key.getBytes()[0];

        for (int i = 0; i < chars.length; i++) {
            chars[i] += encrypt ? shift : -shift;
        }
        return String.valueOf(chars);
    }
}
