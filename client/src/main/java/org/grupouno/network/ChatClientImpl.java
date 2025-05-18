package org.grupouno.network;


import org.grupouno.controller.ChatController;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;
import org.grupouno.model.directory.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class ChatClientImpl implements IChatClient, Runnable {
    private final Logger logger = Logger.getLogger(ChatClientImpl.class.getName());
    private final ChatController chatController;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private User currentUser;

    public ChatClientImpl(Socket socket, ChatController chatController) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.outputStream.flush();
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.chatController = chatController;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message message = (Message) inputStream.readObject();
                logger.info("Received message: " + message.type());
                switch (message.type()) {
                    case MESSAGE -> this.chatController.receiveMessage(message);
                    case DIRECTORY -> this.chatController.updateDirectory(message);
                    case ERROR -> logger.warning("Error receiving message: " + message.type());
                    case MESSAGE_ACK, DISCONNECT_ACK, REGISTER_ACK -> {
                        // no se manejan aquí
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.warning("Error receiving message: " + e.getMessage());

                boolean reconectado = tryReconnectToBackup();
                if (!reconectado) {
                    logger.warning("No se pudo reconectar. Cerrando cliente.");
                    this.close();
                    break; // salimos del while -> termina el hilo
                } else {
                    logger.info("Reconexión exitosa. Continuando...");
                    // sigue en el while
                }
            }
        }
    }

    @Override
    public synchronized void sendMessage(Message message) {
        logger.info("Sending message: " + message);
        try {
            outputStream.writeObject(message);
            outputStream.flush(); // Ensure the stream is flushed after writing
            logger.info("Message sent");
        } catch (IOException e) {
            logger.warning("Error during message communication: " + e.getMessage());
        }
    }

    @Override
    public void registerWithServer(String nickname, String ip, int port) {
        Message message = new Message(
                nickname, ip, port, null,
                socket.getInetAddress().getHostAddress(),
                socket.getPort(), null,
                LocalDateTime.now(), MessageType.REGISTER
        );
        this.currentUser = new User(nickname, ip, port);
        this.sendMessage(message);
    }

    @Override
    public synchronized void getConnectedUsers(String nickname) {
        Message message = new Message(
                nickname, socket.getLocalAddress().getHostAddress(),
                socket.getLocalPort(), null,
                socket.getInetAddress().getHostAddress(),
                socket.getPort(), null,
                LocalDateTime.now(), MessageType.GET_DIRECTORY
        );
        this.sendMessage(message);
    }


    @Override
    public void disconnect(String nickname) {
        Message message = new Message(
                nickname, socket.getLocalAddress().getHostAddress(),
                socket.getLocalPort(), null,
                socket.getInetAddress().getHostAddress(),
                socket.getPort(), null,
                LocalDateTime.now(), MessageType.DISCONNECT
        );
        this.sendMessage(message);
    }

    @Override
    public synchronized void close() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
            logger.info("Connection closed");
        } catch (IOException e) {
            logger.warning("Error closing connection: " + e.getMessage());
        }
    }

    @Override
    public boolean tryReconnectToBackup() {
        logger.info("Intentando reconectarse al backup...");
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
            InetAddress monitorAddress = InetAddress.getByName("127.0.0.1");
            byte[] sendData = "GET_PRIMARY".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, monitorAddress, 9998);
            datagramSocket.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            datagramSocket.setSoTimeout(2000);
            datagramSocket.receive(receivePacket);

            String primaryIp = new String(receivePacket.getData(), 0, receivePacket.getLength());
            logger.info("Nuevo primario recibido: " + primaryIp);

            if (!primaryIp.equals("NONE")) {
                this.close(); // Cerramos conexión previa

                InetAddress userAddress = InetAddress.getByName(currentUser.ip());
                int newPort = socket.getPort() + 1; // Asumimos que el nuevo primario está en puerto+1
                this.socket = new Socket(primaryIp, newPort, userAddress, currentUser.port());
                this.outputStream = new ObjectOutputStream(socket.getOutputStream());
                this.outputStream.flush();
                this.inputStream = new ObjectInputStream(socket.getInputStream());

                registerWithServer(currentUser.nickname(), currentUser.ip(), currentUser.port());
                return true;
            } else {
                logger.warning("No hay primario disponible.");
            }
        } catch (Exception e) {
            logger.warning("Fallo al reconectar: " + e.getMessage());
        } finally {
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                datagramSocket.close();
            }
        }
        return false;
    }

}
