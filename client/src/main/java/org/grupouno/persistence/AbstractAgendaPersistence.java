package org.grupouno.persistence;

import org.grupouno.model.session.ChatSessionImpl;
import org.grupouno.model.session.IChatSession;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class AbstractAgendaPersistence {

    protected static final Logger logger = Logger.getLogger(AbstractAgendaPersistence.class.getName());
    protected File file;
    protected IChatSession session;

    public AbstractAgendaPersistence(String pathName) throws IOException {
        file = new File(pathName);
        if (!file.isFile())
            file.createNewFile(); //El retorno debería ser siempre TRUE...
        session = ChatSessionImpl.getInstance();
    }

    public abstract void saveAgenda();

    public abstract void loadAgenda();
}
