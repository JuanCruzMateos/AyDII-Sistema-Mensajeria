package org.grupouno.network;

import org.grupouno.model.conversation.Message;

public interface IChatClient {
    void sendMessage(Message message); // Renamed from sendAndReceiveMessage

    void registerWithServer(String nickname, String ip, int port); // Renamed from registerClientWithServer

    void getConnectedUsers(String nickname); // Renamed from fetchConnectedUsers

    void disconnect(String nickname);

    void close();
}
