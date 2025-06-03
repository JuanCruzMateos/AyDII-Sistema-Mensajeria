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
            writer.write("<?xml version=\"1.0\"?>\n");
            writer.write("<session>\n");

            logger.info("Guardando agenda");
            writer.write("\t<agenda>\n");
            IDirectory agenda = session.getAgenda();
            for (User act : agenda.getAllContacts()) {
                writer.write("\t\t<user nickname=\"" + act.nickname() + "\" />\n");
            }
            writer.write("\t</agenda>\n");
            logger.info("Guardando conversaciones");
            writer.write("\t<conversations>\n");
            IConversationService convService = session.getConversationService();
            for (User act : agenda.getAllContacts()) {
                if (convService.existsConversationWith(act.nickname())) {
                    writer.write("\t\t<conversation user=\"" + act.nickname() + "\">\n");
                    for (Message mes : convService.getConversationByContactNickname(act.nickname()).orElseThrow().getMessages()) {
                        writer.write("\t\t\t<message>\n");
                        writer.write("\t\t\t\t<sender>" + mes.senderNickname() + "</sender>\n");
                        writer.write("\t\t\t\t<time>" + mes.timestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</time>\n");
                        writer.write("\t\t\t\t<body>" + mes.content().toString() + "</body>\n");
                        writer.write("\t\t\t</message>\n");
                    }
                    writer.write("\t\t</conversation>\n");
                }
            }
            writer.write("\t</conversations>\n");

            writer.write("</session>");
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
            //<?xml version="1.0"?>
            String line = reader.readLine().trim();
            if (!line.equals("<?xml version=\"1.0\"?>")) throw new IOException("XML Mal formateado! - XML Version");
            line = reader.readLine().trim();
            if (!line.equals("<session>")) throw new IOException("XML Mal formateado! - Session");

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

            logger.info("Cargando conversaciones");
            line = reader.readLine().trim();
            if (!line.equals("<conversations>")) throw new IOException("XML Mal formateado! - Conversations");
            line = reader.readLine().trim();
            while (!line.equals("</conversations>")) {
                //<conversation user="USER">
                String pre = line.substring(0, 20);
                String nickname = line.substring(20, line.length() - 2);
                String post = line.substring(line.length() - 2);
                // No debería fallar, pero por las dudas...
                if (!pre.equals("<conversation user=\"") || !post.equals("\">"))
                    throw new IOException("XML Mal formateado! - Conversation");
                logger.info("\tCargando conversacion con " + nickname);
                session.getConversationService().startNewConversation(nickname);
                line = reader.readLine().trim();
                while (!line.equals("</conversation>")) {
                    if (!line.equals("<message>")) throw new IOException("XML Mal formateado! - Message");
                    //<sender>SENDER</sender>
                    line = reader.readLine().trim();
                    pre = line.substring(0, 8);
                    String sender = line.substring(8, line.length() - 9);
                    post = line.substring(line.length() - 9);
                    if (!pre.equals("<sender>") || !post.equals("</sender>"))
                        throw new IOException("XML Mal formateado! - Sender");

                    //<time>dd/MM/yyyy HH:mm:ss</time>
                    line = reader.readLine().trim();
                    pre = line.substring(0, 6);
                    String date = line.substring(6, line.length() - 7);
                    LocalDateTime timestamp = this.getDateTimeFromSTR(date);
                    post = line.substring(line.length() - 7);
                    if (!pre.equals("<time>") || !post.equals("</time>"))
                        throw new IOException("XML Mal formateado! - Time");

                    //<body>BODY</body>
                    line = reader.readLine().trim();
                    pre = line.substring(0, 6);
                    String body = line.substring(6, line.length() - 7);
                    post = line.substring(line.length() - 7);
                    if (!pre.equals("<body>") || !post.equals("</body>"))
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

            line = reader.readLine().trim();
            if (!line.equals("</session>")) throw new IOException("XML Mal formateado! - /Session");

            reader.close();
        } catch (IOException e) {
            logger.severe("Error al cargar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }
}
