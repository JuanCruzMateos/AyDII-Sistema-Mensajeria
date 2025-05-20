package org.grupouno.persistence;

public class JSONSessionPersistence extends FileSessionPersistence {

    public JSONSessionPersistence(String pathName) {
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
