package org.grupouno.network.handlers;

import org.grupouno.model.protocols.Message;
import org.grupouno.model.protocols.MessageType;

import java.io.IOException;

public interface IClientHandler {
    // userConnect
    void registerNewConnection(Message message);

    // userDisconnect
    void removeConnection(String nickname) throws IOException;

    void sendPendingMessages(String nickname);

    void addMessageToPendingMessages(Message message);

    void forwardMessage(Message message);

    void getDirectoryContacts(Message message);

    void serverResponse(String nickname, Object content, MessageType type);
}
