package org.grupouno.persistence;

import java.io.IOException;

public class XMLSessionPersistence extends FileSessionPersistence {

    public XMLSessionPersistence(String fileName) throws IOException {
        super(fileName + ".xml");
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
