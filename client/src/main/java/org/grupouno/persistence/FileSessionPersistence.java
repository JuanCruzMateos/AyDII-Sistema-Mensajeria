package org.grupouno.persistence;

public abstract class FileSessionPersistence implements ISessionPersistence {
    protected String pathName;

    public FileSessionPersistence(String pathName) {
        this.pathName = pathName;
    }
}
