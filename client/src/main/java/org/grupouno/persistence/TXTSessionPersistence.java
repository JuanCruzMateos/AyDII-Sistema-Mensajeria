package org.grupouno.persistence;

import java.io.IOException;

public class TXTSessionPersistence extends FileSessionPersistence {

    public TXTSessionPersistence(String fileName) throws IOException {
        super(fileName + ".txt");
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
