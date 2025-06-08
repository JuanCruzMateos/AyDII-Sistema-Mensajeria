package org.grupouno.persistence;

import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;
import org.grupouno.model.protocols.Message;
import org.grupouno.model.protocols.MessageType;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TXTChatPersistence extends AbstractChatPersistence {

    public TXTChatPersistence(String fileName) throws IOException {
        super(fileName + "_Chat.txt");
    }

    @Override
    public void saveChat() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));

            IDirectory agenda = session.getAgenda();
            logger.info("Guardando conversaciones");
            writer.write("CONVERSACIONES\n");
            IConversationService convService = session.getConversationService();
            for (User act : agenda.getAllContacts()) {
                if (convService.existsConversationWith(act.nickname())) {
                    writer.write(act.nickname() + "\n");
                    for (Message mes : convService.getConversationByContactNickname(act.nickname()).orElseThrow().getMessages()) {
                        writer.write("\t" + mes.senderNickname());
                        writer.write("\t" + mes.timestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                        writer.write("\t" + mes.content().toString() + "\n");
                    }
                }
            }
            writer.close();
            logger.info("TXT Chat Finalizado");
        } catch (IOException e) {
            logger.severe("Error al guardar archivo - LLegar a este lugar es crítico, el archivo debería existir y no estar bloqueado...");
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadChat() {
        //line.split("\t") para separar las 3 partes de los mensajes
        logger.info("Cargando sesión desde TXT");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));

            logger.info("Cargando conversaciones");
            String line = reader.readLine();
            if (!line.equals("CONVERSACIONES")) throw new IOException("TXT Mal formateado! - Conversaciones");
            line = reader.readLine();
            while (line != null) {
                //USER
                String nickname = line;
                logger.info("\tCargando conversacion con " + nickname);
                session.getConversationService().startNewConversation(nickname);
                line = reader.readLine();
                while (line != null && !line.equals(line.trim())) {
                    line = line.trim();
                    //SENDER \t TIME \t BODY
                    String[] tokens = line.split("\t");
                    if (tokens.length != 3) throw new IOException("TXT Mal formateado! - Mensaje");

                    String sender = tokens[0];
                    LocalDateTime timestamp = this.getDateTimeFromSTR(tokens[1]);
                    String body = tokens[2];

                    //Todos los atributos que no son cargados no le importan al usuario en este momento
                    Message mensaje = new Message(sender, "", 0, "", "", 0,
                            body,
                            timestamp,
                            MessageType.MESSAGE);

                    session.getConversationService().addMessage(mensaje, nickname);

                    //Empieza próximo mensaje, o termina la conversacion y empieza la próxima (o termina el archivo)
                    line = reader.readLine();
                }
                logger.info("\tCargada conversacion con " + nickname);
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
