package org.grupouno.server;

import org.grupouno.model.conversation.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface IClientHandler {
    Message listenForMessage(ObjectInputStream in) throws IOException, ClassNotFoundException;
    
    void registerNewConnectionToDirectory(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException;

    void removeConnectionFromDirectory(String nickname);

    void checkForPendingMessages(String nickname, ObjectOutputStream out);

    void addMessageToPendingMessages(Message message);

    void sendMessageToClient(Message message, ObjectOutputStream out) throws IOException;
}
