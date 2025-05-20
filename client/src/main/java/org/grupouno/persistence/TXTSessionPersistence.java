package org.grupouno.persistence;

import java.io.IOException;

public class TXTSessionPersistence extends FileSessionPersistence {

    public TXTSessionPersistence(String pathName) throws IOException {
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
