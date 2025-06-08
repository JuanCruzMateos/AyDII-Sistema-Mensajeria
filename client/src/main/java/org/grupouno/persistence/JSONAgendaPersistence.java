package org.grupouno.persistence;

import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;

import java.io.*;

public class JSONAgendaPersistence extends AbstractAgendaPersistence {

    public JSONAgendaPersistence(String fileName) throws IOException {
        super(fileName + "_Agenda.json");
    }

    @Override
    public void saveAgenda() {
        logger.info("Guardando sesión en JSON");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write("{\n");

            logger.info("Guardando agenda");
            writer.write("\t\"agenda\": [\n");
            IDirectory agenda = session.getAgenda();
            String openBracket = "\t\t{\n";
            for (User act : agenda.getAllContacts()) {
                writer.write(openBracket);
                //"user": "USER"
                writer.write("\t\t\t\"user\": \"" + act.nickname() + "\"\n");
                writer.write("\t\t}");
                openBracket = ",\n\t\t{\n";
            }
            if (openBracket.equals(",\n\t\t{\n"))
                writer.write("\n");
            writer.write("\t]\n");
            writer.write("}");
            writer.close();
            logger.info("JSON Agenda Finalizado");
        } catch (IOException e) {
            logger.severe("Error al guardar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadAgenda() {
        logger.info("Cargando sesión desde JSON");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine().trim();
            if (!line.equals("{")) throw new IOException("JSON Mal formateado! - { principal");

            logger.info("Cargando agenda");
            line = reader.readLine().trim();
            if (!line.equals("\"agenda\": [")) throw new IOException("JSON Mal formateado! - Agenda");
            line = reader.readLine().trim(); //Lee el primer { o el ],
            while (!line.equals("]")) {
                //"user": "USER"
                line = reader.readLine().trim();
                String pre = line.substring(0, 9);
                String nickname = line.substring(9, line.length() - 1);
                String post = line.substring(line.length() - 1);
                // No debería fallar, pero por las dudas...
                if (!pre.equals("\"user\": \"") || !post.equals("\""))
                    throw new IOException("JSON Mal formateado! - User");
                //No es necesario el IP y el Puerto para el usuario...
                logger.info("\tCargado " + nickname);
                session.getAgenda().addContact(new User(nickname, "", 0));
                line = reader.readLine().trim(); //Lee el } o },
                if (!line.equals("}") && !line.equals("},")) throw new IOException("JSON Mal formateado! - Agenda }");
                line = reader.readLine().trim(); //Lee el { o el ],
            }
            logger.info("Agenda cargada");

            line = reader.readLine().trim();
            if (!line.equals("}")) throw new IOException("JSON Mal formateado! - } principal");

            reader.close();
        } catch (IOException e) {
            logger.severe("Error al cargar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }
}
