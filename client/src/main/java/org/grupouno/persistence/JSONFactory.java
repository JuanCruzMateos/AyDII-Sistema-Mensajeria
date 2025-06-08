package org.grupouno.persistence;

import java.io.IOException;

public class JSONFactory implements IPersistenceFactory {
    @Override
    public AbstractAgendaPersistence getAgendaPersistence(String username) throws IOException {
        return new JSONAgendaPersistence(username);
    }

    @Override
    public AbstractChatPersistence getChatPersistence(String username) throws IOException {
        return new JSONChatPersistence(username);
    }
}
