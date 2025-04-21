package org.grupouno.network.connections;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public record ConnectionManager(
        Socket socket,
        ObjectOutputStream objectOutputStream,
        ObjectInputStream objectInputStream
) {

}
