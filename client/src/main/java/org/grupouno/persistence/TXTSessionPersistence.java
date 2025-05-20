package org.grupouno.persistence;

public class TXTSessionPersistence extends FileSessionPersistence {

    public TXTSessionPersistence(String pathName) {
        super(pathName);
    }

    @Override
    public void saveSession() {
        throw new UnsupportedOperationException("No implementado todavia");
    }

    @Override
    public void loadSession() {
        throw new UnsupportedOperationException("No implementado todavia");
    }
}
