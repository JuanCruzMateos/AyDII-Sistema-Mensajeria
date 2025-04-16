package org.grupouno.network;

import org.grupouno.model.conversation.Message;

import java.util.List;

public interface IChatClient {
    Message postMessage(Message message);

    void sendMessage(String nickname, String ip, int port, String content);

    void registerClient(String nickname, String ip, int port);

    List<String> getDirectory(String nickname, String ip, int port);

    void close();
}
