package org.grupouno.model.session;

import org.grupouno.model.agenda.User;
import org.grupouno.model.conversation.Message;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ChatSessionTest {
    @Test
    public void getInstanceReturnsSameInstance() {
        ChatSession instance1 = ChatSession.getInstance();
        ChatSession instance2 = ChatSession.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void getMessagesByContactReturnsEmptyStringForNewConversation() {
        ChatSession session = ChatSession.getInstance();
        session.initConversationService();
        String messages = session.getMessagesByContact("new_contact");
        assertEquals("", messages);
    }

    @Test
    public void addNewContactAddsContactToAgenda() {
        ChatSession session = ChatSession.getInstance();
        session.initAgenda();
        User newUser = new User("new_user", "192.168.1.2", 8081);
        session.addNewContact(newUser);
        assertTrue(session.getAgendaContacts().contains("new_user"));
    }

    @Test
    public void existsConversationWithReturnsTrueForExistingConversation() {
        ChatSession session = ChatSession.getInstance();
        session.initConversationService();
        session.startNewConversation("existing_contact");
        assertTrue(session.existsConversationWith("existing_contact"));
    }

    @Test
    public void sendMessageAddsMessageToConversation() {
        ChatSession session = ChatSession.getInstance();
        session.initConversationService();
        session.startNewConversation("receiver");
        Message message = new Message(
                "sender",
                "127.0.0.1",
                8080,
                "receiver",
                "127.0.0.2",
                8082,
                "Hello?",
                LocalDateTime.now()

        );
        session.sendMessage(message);
        String messages = session.getMessagesByContact("receiver");
        assertTrue(messages.contains("Hello"));
    }

    @Test
    public void receiveMessageAddsNewContactIfNotInAgenda() {
        ChatSession session = ChatSession.getInstance();
        session.initAgenda();
        session.initConversationService();
        Message message = new Message(
                "unknown_sender",
                "127.0.0.1",
                8080,
                "receiver",
                "127.0.0.2",
                8082,
                "Hello?",
                LocalDateTime.now()

        );
        session.receiveMessage(message);
        assertTrue(session.getAgendaContacts().contains("unknown_sender"));
    }

    @Test
    public void receiveMessageStartsNewConversationIfNotExists() {
        ChatSession session = ChatSession.getInstance();
        session.initAgenda();
        session.initConversationService();
        Message message = new Message(
                "sender",
                "127.0.0.1",
                8080,
                "receiver",
                "127.0.0.2",
                8082,
                "Hello?",
                LocalDateTime.now()

        );
        session.receiveMessage(message);
        assertTrue(session.existsConversationWith("sender"));
    }
}
