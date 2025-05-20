package org.grupouno.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public abstract class FileSessionPersistence implements ISessionPersistence {
    protected FileReader reader = null;
    File file;

    public FileSessionPersistence(String pathName) throws IOException {
        file = new File(pathName);
        if (!file.isFile())
            file.createNewFile();
    }

    protected void openFile() throws FileNotFoundException {
        reader = new FileReader(file);
        
    }

    protected void closeFile() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
