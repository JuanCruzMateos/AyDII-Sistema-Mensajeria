package org.grupouno.persistence;

public interface ISessionPersistence {

    void saveSession(); // Parámetros?
    
    void loadSession(String user); // Retorno?

}
