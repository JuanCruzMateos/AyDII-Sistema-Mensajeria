package org.grupouno.persistence;

import java.io.IOException;

public class JSONSessionPersistence extends FileSessionPersistence {

    public JSONSessionPersistence(String pathName) throws IOException {
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
