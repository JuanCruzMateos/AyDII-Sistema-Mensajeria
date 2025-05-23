package org.grupouno.persistence;

import java.io.File;
import java.io.IOException;

public abstract class FileSessionPersistence implements ISessionPersistence {
    File file;

    public FileSessionPersistence(String pathName) throws IOException {
        file = new File(pathName);
        if (!file.isFile())
            file.createNewFile(); //El retorno debería ser siempre TRUE...
    }
}
