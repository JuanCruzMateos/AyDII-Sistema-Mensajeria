package org.grupouno.persistence;

import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;

import java.io.*;

public class TXTAgendaPersistence extends AbstractAgendaPersistence {

    public TXTAgendaPersistence(String fileName) throws IOException {
        super(fileName + "_Agenda.txt");
    }

    @Override
    public void saveAgenda() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));

            logger.info("Guardando agenda");
            writer.write("AGENDA\n");
            IDirectory agenda = session.getAgenda();
            for (User act : agenda.getAllContacts()) {
                writer.write("\t" + act.nickname() + "\n");
            }
            writer.close();
            logger.info("TXT Agenda Finalizado");
        } catch (IOException e) {
            logger.severe("Error al guardar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadAgenda() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));

            logger.info("Cargando agenda");
            String line = reader.readLine().trim();
            if (!line.equals("AGENDA")) throw new IOException("TXT Mal formateado! - Agenda");
            line = reader.readLine();
            while (line != null) {
                //\tUSER
                String nickname = line.trim();
                //No es necesario el IP y el Puerto para el usuario...
                logger.info("\tCargado " + nickname);
                session.getAgenda().addContact(new User(nickname, "", 0));
                line = reader.readLine();
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
