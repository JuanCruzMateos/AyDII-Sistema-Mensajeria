package org.grupouno.network;

import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class ChatClientImpl implements IChatClient {
    private final Logger logger = Logger.getLogger(ChatClientImpl.class.getName());
    private final String serverIp;
    private final int serverPort;
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;

    public ChatClientImpl(Socket socket) {
        this.socket = socket;
        this.serverIp = socket.getInetAddress().getHostAddress();
        this.serverPort = socket.getPort();
        try {
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message postMessage(Message message) {
        Message response = null;
        try {
            outputStream.writeObject(message);
            outputStream.flush();
            logger.info("Message sent: " + message);
        } catch (IOException e) {
            logger.warning("Error sending message: " + e.getMessage());
        }
        try {
            response = (Message) this.inputStream.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Error reading response: " + e.getMessage());
        }
        return response;
    }

    @Override
    public void sendMessage(String nickname, String ip, int port, String content) {
        Message message = new Message(nickname, ip, port, null, this.serverIp, this.serverPort, content, LocalDateTime.now(), MessageType.MESSAGE);
        Message response = this.postMessage(message);
        logger.info("Response received: " + response);
    }

    @Override
    public void registerClient(String nickname, String ip, int port) {
        Message message = new Message(nickname, ip, port, null, this.serverIp, this.serverPort, null, LocalDateTime.now(), MessageType.REGISTER);
        Message response = this.postMessage(message);
        logger.info("Response received: " + response);
    }

    @Override
    public List<String> getDirectory(String nickname, String ip, int port) {
        Message message = new Message(nickname, ip, port, null, this.serverIp, this.serverPort, null, LocalDateTime.now(), MessageType.GET_DIRECTORY);
        Message response = this.postMessage(message);
        logger.info("Response received: " + response);
        if (response != null && response.type() == MessageType.DIRECTORY) {
            String[] connectedUsers = response.content().split(",");
            logger.info("Connected users: " + String.join(", ", connectedUsers));
            return List.of(connectedUsers);
        } else {
            logger.warning("Error getting directory: " + (response != null ? response.content() : "No response"));
            return null;
        }
    }

    @Override
    public void close() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            logger.info("Connection closed");
        } catch (Exception e) {
            logger.warning("Error closing connection: " + e.getMessage());
        }
    }
}