package org.grupouno.persistence;

import java.io.File;
import java.io.IOException;

public class PersistenceFactory {

    public static final int XML_PERSISTENCE = 1;
    public static final int JSON_PERSISTENCE = 2;
    public static final int TXT_PERSISTENCE = 3;
    public static final String folder = "saves/";

    private static void ensureFolderExists() {
        File f = new File(folder);
        if (!f.isDirectory())
            f.mkdir(); // El resultado debería ser siempre TRUE si entró acá.
    }

    public static ISessionPersistence getPersistence(String username) throws IOException {
        ensureFolderExists();
        String filePath = folder + username;
        ISessionPersistence ans = null;
        if (new File(filePath + ".xml").isFile())
            ans = new XMLSessionPersistence(filePath);
        else if (new File(filePath + ".json").isFile())
            ans = new JSONSessionPersistence(filePath);
        else if (new File(filePath + ".txt").isFile())
            ans = new TXTSessionPersistence(filePath);
        return ans;
    }

    public static ISessionPersistence getPersistence(String username, int type) throws IOException {
        ensureFolderExists();
        String filePath = folder + username;
        ISessionPersistence ans = switch (type) {
            case XML_PERSISTENCE -> new XMLSessionPersistence(filePath);
            case JSON_PERSISTENCE -> new JSONSessionPersistence(filePath);
            case TXT_PERSISTENCE -> new TXTSessionPersistence(filePath);
            default -> null;
        };
        return ans;
    }
}