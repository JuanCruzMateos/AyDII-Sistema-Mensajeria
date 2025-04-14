package org.grupouno.model.conversation;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ConversationServiceTest {
    @Test
    public void existsConversationWithReturnsTrueForExistingConversation() {
        ConversationService service = new ConversationService(new HashMap<>());
        service.startNewConversation("john_doe");
        assertTrue(service.existsConversationWith("john_doe"));
    }

    @Test
    public void existsConversationWithReturnsFalseForNonExistingConversation() {
        ConversationService service = new ConversationService(new HashMap<>());
        assertFalse(service.existsConversationWith("non_existent"));
    }

    @Test
    public void getConversationByContactNicknameReturnsCorrectConversation() {
        ConversationService service = new ConversationService(new HashMap<>());
        service.startNewConversation("john_doe");
        assertNotNull(service.getConversationByContactNickname("john_doe"));
    }

    @Test
    public void getConversationByContactNicknameReturnsNullForNonExistingConversation() {
        ConversationService service = new ConversationService(new HashMap<>());
        assertNull(service.getConversationByContactNickname("non_existent"));
    }

    @Test
    public void startNewConversationCreatesNewConversation() {
        ConversationService service = new ConversationService(new HashMap<>());
        service.startNewConversation("john_doe");
        assertTrue(service.existsConversationWith("john_doe"));
    }

    @Test
    public void addMessageAddsMessageToExistingConversation() {
        ConversationService service = new ConversationService(new HashMap<>());
        service.startNewConversation("john_doe");
        Message message = new Message(
                "user1",
                "127.0.0.1",
                8080,
                "john_doe",
                "127.0.0.2",
                8082,
                "Hello, how are you?",
                LocalDateTime.now()

        );
        service.addMessage(message, "john_doe");
        assertTrue(service.getConversationByContactNickname("john_doe").messages().contains(message));
    }
}
