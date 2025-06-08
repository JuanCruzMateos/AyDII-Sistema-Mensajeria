package org.grupouno.persistence;

import java.io.IOException;

public class XMLFactory implements IPersistenceFactory {
    @Override
    public AbstractAgendaPersistence getAgendaPersistence(String username) throws IOException {
        return new XMLAgendaPersistence(username);
    }

    @Override
    public AbstractChatPersistence getChatPersistence(String username) throws IOException {
        return new XMLChatPersistence(username);
    }
}
