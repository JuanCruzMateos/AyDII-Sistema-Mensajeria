package org.grupouno.persistence;

import java.io.IOException;

public class XMLSessionPersistence extends FileSessionPersistence {

    public XMLSessionPersistence(String pathName) throws IOException {
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
