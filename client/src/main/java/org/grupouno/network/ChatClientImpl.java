package org.grupouno.network;

import org.grupouno.controller.ChatController;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class ChatClientImpl implements IChatClient, Runnable {
    private final Logger logger = Logger.getLogger(ChatClientImpl.class.getName());
    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final ChatController chatController;

    public ChatClientImpl(Socket socket, ChatController chatController) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.chatController = chatController;
    }

    @Override
    public void run() {
        try {
            Message message = (Message) this.inputStream.readObject();
            while (message.type() != MessageType.DISCONNECT_ACK) {
                logger.info("Received message: " + message.type());
                this.handleMessage(message);
                message = (Message) this.inputStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            this.logger.warning("Error receiving message: " + e.getMessage());
        } finally {
            this.logger.info("Received DISCONNECT_ACK, stopping client thread");
            this.close();
        }
    }

    private void handleMessage(Message message) {
        switch (message.type()) {
            case MESSAGE -> this.chatController.receiveMessage(message);
            case DIRECTORY -> this.chatController.updateDirectory(message);
            case ERROR -> this.logger.warning("Error receiving message: " + message.type());
            case MESSAGE_ACK, REGISTER_ACK -> {
                // Handle acknowledgment messages
            }
            default -> this.logger.warning("Unkwnon message type: " + message.type());
        }
    }

    private Message createMessage(String nickname, String ip, int port, MessageType type) {
        return new Message(
                nickname, ip, port,
                null, this.socket.getInetAddress().getHostAddress(), this.socket.getPort(),
                null, LocalDateTime.now(), type
        );
    }

    @Override
    public synchronized void sendMessage(Message message) {
        try {
            this.outputStream.writeObject(message);
            this.outputStream.flush();
            this.logger.info("Message sent: " + message);
        } catch (IOException e) {
            this.logger.warning("Error sending message: " + e.getMessage());
        }
    }

    @Override
    public void registerWithServer(String nickname, String ip, int port) {
        this.sendMessage(this.createMessage(nickname, ip, port, MessageType.REGISTER));
    }

    @Override
    public synchronized void getConnectedUsers(String nickname) {
        this.sendMessage(this.createMessage(nickname, socket.getLocalAddress().getHostAddress(), socket.getLocalPort(), MessageType.GET_DIRECTORY));
    }

    @Override
    public synchronized void disconnect(String nickname) {
        this.sendMessage(this.createMessage(nickname, socket.getLocalAddress().getHostAddress(), socket.getLocalPort(), MessageType.DISCONNECT));
    }

    private synchronized void close() {
        try {
            if (this.inputStream != null) {
                this.inputStream.close();
                this.logger.info("Input stream closed");
            } else {
                this.logger.info("Input stream is null");
            }
            if (this.outputStream != null) {
                this.outputStream.close();
                this.logger.info("Output stream closed");
            } else {
                this.logger.info("Output stream is null");
            }
            if (this.socket != null && !this.socket.isClosed()) {
                this.logger.info("Closing socket: " + this.socket);
                this.socket.setReuseAddress(Boolean.TRUE);
                this.socket.close();
            }
        } catch (IOException e) {
            this.logger.warning("Error closing connection: " + e.getMessage());
        }
    }
}