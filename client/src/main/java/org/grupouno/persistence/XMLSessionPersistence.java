package org.grupouno.persistence;

import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;
import org.grupouno.model.protocols.Message;
import org.grupouno.model.protocols.MessageType;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class XMLSessionPersistence extends FileSessionPersistence {

    public XMLSessionPersistence(String fileName) throws IOException {
        super(fileName + ".xml");
    }

    @Override
    public void saveSession() {
        logger.info("Guardando sesión en XML");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));

            logger.info("Guardando agenda");
            writer.write("<agenda>\n");
            IDirectory agenda = session.getAgenda();
            for (User act : agenda.getAllContacts()) {
                writer.write("\t<user nickname=\"" + act.nickname() + "\" />\n");
            }
            writer.write("</agenda>\n");
            logger.info("Guardando conversaciones");
            writer.write("<conversations>\n");
            IConversationService convService = session.getConversationService();
            for (User act : agenda.getAllContacts()) {
                if (convService.existsConversationWith(act.nickname())) {
                    writer.write("\t<conversation nickname=\"" + act.nickname() + "\">\n");
                    for (Message mes : convService.getConversationByContactNickname(act.nickname()).get().getMessages()) {
                        writer.write("\t\t<message>\n");
                        writer.write("\t\t\t<sender \"" + mes.senderNickname() + "\" />\n");
                        writer.write("\t\t\t<time \"" + mes.timestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\" />\n");
                        writer.write("\t\t\t<body \"" + mes.content().toString() + "\" />\n");
                        writer.write("\t\t</message>\n");
                    }
                    writer.write("\t</conversation>\n");
                }
            }
            writer.write("</conversations>");
            writer.close();
            logger.info("XML Finalizado");
        } catch (IOException e) {
            logger.severe("Error al guardar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadSession() {
        logger.info("Cargando sesión desde XML");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));

            logger.info("Cargando agenda");
            String line = reader.readLine().trim();
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

            logger.info("Cargando conversaciones");
            line = reader.readLine().trim();
            if (!line.equals("<conversations>")) throw new IOException("XML Mal formateado! - Conversations");
            line = reader.readLine().trim();
            while (!line.equals("</conversations>")) {
                //<conversation nickname="USER">
                String pre = line.substring(0, 24);
                String nickname = line.substring(24, line.length() - 2);
                String post = line.substring(line.length() - 2);
                // No debería fallar, pero por las dudas...
                if (!pre.equals("<conversation nickname=\"") || !post.equals("\">"))
                    throw new IOException("XML Mal formateado! - Conversation");
                logger.info("\tCargando conversacion con " + nickname);
                session.getConversationService().startNewConversation(nickname);
                line = reader.readLine().trim();
                while (!line.equals("</conversation>")) {
                    if (!line.equals("<message>")) throw new IOException("XML Mal formateado! - Message");
                    //<sender "SENDER" />
                    line = reader.readLine().trim();
                    pre = line.substring(0, 9);
                    String sender = line.substring(9, line.length() - 4);
                    post = line.substring(line.length() - 4);
                    if (!pre.equals("<sender \"") || !post.equals("\" />"))
                        throw new IOException("XML Mal formateado! - Sender");

                    //<time "dd/MM/yyyy HH:mm:ss" />
                    line = reader.readLine().trim();
                    pre = line.substring(0, 7);
                    String date = line.substring(7, line.length() - 4);
                    LocalDateTime timestamp = this.getDateTimeFromSTR(date);
                    post = line.substring(line.length() - 4);
                    if (!pre.equals("<time \"") || !post.equals("\" />"))
                        throw new IOException("XML Mal formateado! - Time");

                    //<body "BODY\" />
                    line = reader.readLine().trim();
                    pre = line.substring(0, 7);
                    String body = line.substring(7, line.length() - 4);
                    post = line.substring(line.length() - 4);
                    if (!pre.equals("<body \"") || !post.equals("\" />"))
                        throw new IOException("XML Mal formateado! - Body");

                    //Todos los atributos que no son cargados no le importan al usuario en este momento
                    Message mensaje = new Message(sender, "", 0, "", "", 0,
                            body,
                            timestamp,
                            MessageType.MESSAGE);

                    session.getConversationService().addMessage(mensaje, nickname);

                    line = reader.readLine().trim();
                    if (!line.equals("</message>")) throw new IOException("XML Mal formateado! - /Message");

                    //Empieza próximo mensaje, o termina la conversacion
                    line = reader.readLine().trim();
                }
                logger.info("\tCargada conversacion con " + nickname);

                //Empieza próxima conversación, o terminan las conversaciones
                line = reader.readLine().trim();
            }
            logger.info("Conversaciones cargadas");


            reader.close();
        } catch (IOException e) {
            logger.severe("Error al cargar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }
}
