package org.grupouno.persistence;

public class XMLSessionPersistence extends FileSessionPersistence {

    public XMLSessionPersistence(String pathName) {
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
