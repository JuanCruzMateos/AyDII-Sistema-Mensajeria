package org.grupouno.persistence;

import java.io.File;
import java.io.IOException;

public class PersistenceFactory {

    public static final int XML_PERSISTENCE = 1;
    public static final int JSON_PERSISTENCE = 2;
    public static final int TXT_PERSISTENCE = 3;
    public static final String path = "/saves/";


    public ISessionPersistence getPersistence(String username) throws IOException {
        String filePath = path + username;
        if (new File(filePath + ".xml").isFile())
            return new XMLSessionPersistence(filePath);
        else if (new File(filePath + ".json").isFile())
            return new JSONSessionPersistence(filePath);
        else if (new File(filePath + ".txt").isFile())
            return new TXTSessionPersistence(filePath);
        return null;
    }

    public ISessionPersistence getPersistence(String username, int type) throws IOException {
        String filePath = path + username;
        ISessionPersistence ans = switch (type) {
            case XML_PERSISTENCE -> new XMLSessionPersistence(filePath);
            case JSON_PERSISTENCE -> new JSONSessionPersistence(filePath);
            case TXT_PERSISTENCE -> new TXTSessionPersistence(filePath);
            default -> null;
        };
        return ans;
    }
}
