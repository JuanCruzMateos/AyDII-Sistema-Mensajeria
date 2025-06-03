package org.grupouno.persistence;

import org.grupouno.model.session.ChatSessionImpl;
import org.grupouno.model.session.IChatSession;

import java.io.File;
import java.io.IOException;

public abstract class FileSessionPersistence implements ISessionPersistence {
    protected File file;
    protected IChatSession session;

    public FileSessionPersistence(String pathName) throws IOException {
        file = new File(pathName);
        if (!file.isFile())
            file.createNewFile(); //El retorno debería ser siempre TRUE...
        session = ChatSessionImpl.getInstance();
    }
}
