package org.grupouno.persistence;

import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;
import org.grupouno.model.protocols.Message;
import org.grupouno.model.protocols.MessageType;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JSONSessionPersistence extends FileSessionPersistence {

    public JSONSessionPersistence(String fileName) throws IOException {
        super(fileName + ".json");
    }

    @Override
    public void saveSession() {
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
            writer.write("\t],\n");
            logger.info("Guardando conversaciones");
            writer.write("\t\"conversations\": [\n");
            IConversationService convService = session.getConversationService();
            openBracket = "\t\t{\n";
            for (User act : agenda.getAllContacts()) {
                writer.write(openBracket);
                if (convService.existsConversationWith(act.nickname())) {
                    //"user": "USER"
                    writer.write("\t\t\t\"user\": \"" + act.nickname() + "\"\n");
                    //"messages": [
                    writer.write("\t\t\t\"messages\": [\n");
                    openBracket = "\t\t\t\t{\n";
                    for (Message mes : convService.getConversationByContactNickname(act.nickname()).orElseThrow().getMessages()) {
                        writer.write(openBracket);
                        writer.write("\t\t\t\t\t\"sender\": \"" + mes.senderNickname() + "\",\n");
                        writer.write("\t\t\t\t\t\"time\": \"" + mes.timestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\",\n");
                        writer.write("\t\t\t\t\t\"body\": \"" + mes.content().toString() + "\"\n");
                        writer.write("\t\t\t\t}");
                        openBracket = ",\n\t\t\t\t{\n";
                    }
                    if (openBracket.equals(",\n\t\t\t\t{\n"))
                        writer.write("\n");
                    writer.write("\t\t\t],\n");
                }
                writer.write("\t\t}");
                openBracket = ",\n\t\t{\n";
            }
            if (openBracket.equals(",\n\t\t{\n"))
                writer.write("\n");
            writer.write("\t]\n");

            writer.write("}");
            writer.close();
            logger.info("JSON Finalizado");
        } catch (IOException e) {
            logger.severe("Error al guardar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadSession() {
        logger.info("Cargando sesión desde JSON");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine().trim();
            if (!line.equals("{")) throw new IOException("JSON Mal formateado! - Session {");

            logger.info("Cargando agenda");
            line = reader.readLine().trim();
            if (!line.equals("\"agenda\": [")) throw new IOException("JSON Mal formateado! - Agenda");
            line = reader.readLine().trim(); //Lee el primer { o el ],
            while (!line.equals("],")) {
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

            logger.info("Cargando conversaciones");
            line = reader.readLine().trim();
            if (!line.equals("\"conversations\": [")) throw new IOException("JSON Mal formateado! - Conversations");
            line = reader.readLine().trim(); //Lee el primer { o el ]
            while (!line.equals("]")) {
                //"user": "USER",
                line = reader.readLine().trim();
                String pre = line.substring(0, 9);
                String nickname = line.substring(9, line.length() - 2);
                String post = line.substring(line.length() - 2);
                // No debería fallar, pero por las dudas...
                if (!pre.equals("\"user\": \"") || !post.equals("\","))
                    throw new IOException("JSON Mal formateado! - Conversation ~" + line);
                logger.info("\tCargando conversacion con " + nickname);
                line = reader.readLine().trim();
                if (!line.equals("\"messages\": [")) throw new IOException("JSON Mal formateado! - Messages");
                session.getConversationService().startNewConversation(nickname);
                line = reader.readLine().trim(); //Lee el primer { o el ]
                while (!line.equals("]")) {
                    //"sender": "USER",
                    line = reader.readLine().trim();
                    pre = line.substring(0, 11);
                    String sender = line.substring(11, line.length() - 2);
                    post = line.substring(line.length() - 2);
                    if (!pre.equals("\"sender\": \"") || !post.equals("\","))
                        throw new IOException("JSON Mal formateado! - Sender");

                    //"time": "03/06/2025 01:17:19",
                    line = reader.readLine().trim();
                    pre = line.substring(0, 9);
                    String date = line.substring(9, line.length() - 2);
                    LocalDateTime timestamp = this.getDateTimeFromSTR(date);
                    post = line.substring(line.length() - 2);
                    if (!pre.equals("\"time\": \"") || !post.equals("\","))
                        throw new IOException("JSON Mal formateado! - Time");

                    //"body": "CHEE, POSTA YA CARGA LA AGENDA?!?!"
                    line = reader.readLine().trim();
                    pre = line.substring(0, 9);
                    String body = line.substring(9, line.length() - 1);
                    post = line.substring(line.length() - 1);
                    if (!pre.equals("\"body\": \"") || !post.equals("\""))
                        throw new IOException("JSON Mal formateado! - Body");

                    //Todos los atributos que no son cargados no le importan al usuario en este momento
                    Message mensaje = new Message(sender, "", 0, "", "", 0,
                            body,
                            timestamp,
                            MessageType.MESSAGE);

                    session.getConversationService().addMessage(mensaje, nickname);

                    line = reader.readLine().trim(); //Lee el } o },
                    if (!line.equals("}") && !line.equals("},"))
                        throw new IOException("JSON Mal formateado! - Messages }");
                    line = reader.readLine().trim(); //Lee el { o el ]
                }
                logger.info("\tCargada conversacion con " + nickname);

                line = reader.readLine().trim(); //Lee el } o },
                if (!line.equals("}") && !line.equals("},"))
                    throw new IOException("JSON Mal formateado! - Conversations }");
                line = reader.readLine().trim(); //Lee el { o el ]
            }
            logger.info("Conversaciones cargadas");

            line = reader.readLine().trim();
            if (!line.equals("}")) throw new IOException("JSON Mal formateado! - Session }");

            reader.close();
        } catch (IOException e) {
            logger.severe("Error al cargar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }
}
