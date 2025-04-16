package org.grupouno.network;

import org.grupouno.exceptions.ClientNotConnectedException;
import org.grupouno.model.conversation.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface IClientHandler {
    Message listenForNewMessages(ObjectInputStream in) throws IOException, ClassNotFoundException;

    void registerNewConnectionToDirectory(Message message, ObjectOutputStream out);

    void removeConnectionFromDirectory(String nickname);

    void sendPendingMessages(String nickname, ObjectOutputStream out);

    void addMessageToPendingMessages(Message message);

    void sendMessageToClient(Message message, ObjectOutputStream out) throws ClientNotConnectedException;

    void getDirectoryContacts(Message message, ObjectOutputStream out);
}
