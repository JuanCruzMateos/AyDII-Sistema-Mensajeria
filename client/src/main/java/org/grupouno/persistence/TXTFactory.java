package org.grupouno.persistence;

import java.io.IOException;

public class TXTFactory implements IPersistenceFactory {
    @Override
    public AbstractAgendaPersistence getAgendaPersistence(String username) throws IOException {
        return new TXTAgendaPersistence(username);
    }

    @Override
    public AbstractChatPersistence getChatPersistence(String username) throws IOException {
        return new TXTChatPersistence(username);
    }
}
