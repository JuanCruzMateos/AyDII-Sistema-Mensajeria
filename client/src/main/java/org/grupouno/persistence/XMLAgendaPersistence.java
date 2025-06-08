package org.grupouno.persistence;

import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;

import java.io.*;

public class XMLAgendaPersistence extends AbstractAgendaPersistence {

    public XMLAgendaPersistence(String fileName) throws IOException {
        super(fileName + "_Agenda.xml");
    }

    @Override
    public void saveAgenda() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write("<?xml version=\"1.0\"?>\n");

            logger.info("Guardando agenda");
            writer.write("<agenda>\n");
            IDirectory agenda = session.getAgenda();
            for (User act : agenda.getAllContacts()) {
                writer.write("\t<user nickname=\"" + act.nickname() + "\" />\n");
            }
            writer.write("</agenda>\n");

            writer.close();
            logger.info("XML Agenda Finalizado");
        } catch (IOException e) {
            logger.severe("Error al guardar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadAgenda() {
        logger.info("Cargando sesión desde XML");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            //<?xml version="1.0"?>
            String line = reader.readLine().trim();
            if (!line.equals("<?xml version=\"1.0\"?>")) throw new IOException("XML Mal formateado! - XML Version");

            logger.info("Cargando agenda");
            line = reader.readLine().trim();
            if (!line.equals("<agenda>")) throw new IOException("XML Mal formateado! - Agenda");
            line = reader.readLine().trim();
            while (!line.equals("</agenda>")) {
                //<user nickname="USER" />
                String pre = line.substring(0, 16);
                String nickname = line.substring(16, line.length() - 4);
                String post = line.substring(line.length() - 4);
                // No debería fallar, pero por las dudas...
                if (!pre.equals("<user nickname=\"") || !post.equals("\" />"))
                    throw new IOException("XML Mal formateado! - User");
                //No es necesario el IP y el Puerto para el usuario...
                logger.info("\tCargado " + nickname);
                session.getAgenda().addContact(new User(nickname, "", 0));
                line = reader.readLine().trim();
            }
            logger.info("Agenda cargada");

            reader.close();
        } catch (IOException e) {
            logger.severe("Error al cargar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }
}
