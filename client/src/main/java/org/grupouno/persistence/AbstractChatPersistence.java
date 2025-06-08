package org.grupouno.persistence;

import org.grupouno.model.session.ChatSessionImpl;
import org.grupouno.model.session.IChatSession;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public abstract class AbstractChatPersistence {

    protected static final Logger logger = Logger.getLogger(AbstractChatPersistence.class.getName());
    protected File file;
    protected IChatSession session;

    public AbstractChatPersistence(String pathName) throws IOException {
        file = new File(pathName);
        if (!file.isFile())
            file.createNewFile(); //El retorno debería ser siempre TRUE...
        session = ChatSessionImpl.getInstance();
    }

    /**
     * Retorna un LocalDateTime para una fecha pasada por String
     *
     * @param date Fecha formateada "dd/MM/yyyy HH:mm:ss"
     * @return El LocalDateTime de esa fecha
     */
    protected LocalDateTime getDateTimeFromSTR(String date) {
        int day = Integer.parseInt(date.substring(0, 2));
        int month = Integer.parseInt(date.substring(3, 5));
        int year = Integer.parseInt(date.substring(6, 10));
        int hour = Integer.parseInt(date.substring(11, 13));
        int minute = Integer.parseInt(date.substring(14, 16));
        int second = Integer.parseInt(date.substring(17, 19));
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    public abstract void saveChat();

    public abstract void loadChat();
}
