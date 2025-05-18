package org.grupouno.model.connection;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public record ConnectionManager(
        Socket socket,
        ObjectOutputStream objectOutputStream,
        ObjectInputStream objectInputStream
) {

}
