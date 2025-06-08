package org.grupouno.persistence;

import java.io.IOException;

public interface IPersistenceFactory {
    int XML_PERSISTENCE = 1;
    int JSON_PERSISTENCE = 2;
    int TXT_PERSISTENCE = 3;

    AbstractAgendaPersistence getAgendaPersistence(String username) throws IOException;

    AbstractChatPersistence getChatPersistence(String username) throws IOException;
}
