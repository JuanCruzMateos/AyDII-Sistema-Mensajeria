package org.grupouno.persistence;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class PersistenceService {
    public static final int XML_PERSISTENCE = IPersistenceFactory.XML_PERSISTENCE;
    public static final int JSON_PERSISTENCE = IPersistenceFactory.JSON_PERSISTENCE;
    public static final int TXT_PERSISTENCE = IPersistenceFactory.TXT_PERSISTENCE;
    public static final String folder = "saves/";
    private String username = "";
    private IPersistenceFactory persistenceFactory = null;
    private AbstractAgendaPersistence agendaPersistence = null;
    private AbstractChatPersistence chatPersistence = null;

    public PersistenceService(String username) throws IOException {
        this.username = username;
        verifyPersistenceExistence();
    }


    private static void ensureFolderExists() {
        File f = new File(folder);
        if (!f.isDirectory())
            f.mkdir(); // El resultado debería ser siempre TRUE si entró acá.
    }

    public boolean areFilesCreated() {
        return chatPersistence != null;
    }

    private void verifyPersistenceExistence() throws IOException {
        ensureFolderExists();
        String filePath = folder + username;
        if (new File(filePath + "_Agenda.xml").isFile())
            persistenceFactory = new XMLFactory();
        else if (new File(filePath + "_Agenda.json").isFile())
            persistenceFactory = new JSONFactory();
        else if (new File(filePath + "_Agenda.txt").isFile())
            persistenceFactory = new TXTFactory();

        if (persistenceFactory != null)
            loadPersistence();
    }

    public void createPersistence(int type) throws IOException {
        if (!areFilesCreated()) {
            ensureFolderExists();
            String filePath = folder + username;
            switch (type) {
                case XML_PERSISTENCE:
                    persistenceFactory = new XMLFactory();
                    break;
                case JSON_PERSISTENCE:
                    persistenceFactory = new JSONFactory();
                    break;
                case TXT_PERSISTENCE:
                    persistenceFactory = new TXTFactory();
            }
            if (persistenceFactory != null)
                loadPersistence();
        }
    }

    private void loadPersistence() throws IOException {
        String filePath = folder + username;
        agendaPersistence = persistenceFactory.getAgendaPersistence(filePath);
        chatPersistence = persistenceFactory.getChatPersistence(filePath);
    }

    public void saveSession() {
        if (areFilesCreated()) {
            Logger.getLogger(PersistenceService.class.getName()).info("Guardando sesión");
            agendaPersistence.saveAgenda();
            chatPersistence.saveChat();
        } else
            Logger.getLogger(PersistenceService.class.getName()).warning("Sesión no guardada: no hay persistencia creada");
    }

    public void loadSession() {
        if (areFilesCreated()) {
            Logger.getLogger(PersistenceService.class.getName()).info("Cargando sesión");
            agendaPersistence.loadAgenda();
            chatPersistence.loadChat();
        } else
            Logger.getLogger(PersistenceService.class.getName()).warning("Sesión no cargada: no hay persistencia creada");
    }
}