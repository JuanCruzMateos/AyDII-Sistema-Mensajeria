package org.grupouno.model.conversation;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConversationTest {

    @Test
    public void addMessageAppendsMessageToConversation() {
        Message message = new Message(
                "user1",
                "127.0.0.1",
                8080,
                "user2",
                "127.0.0.2",
                8082,
                "Hello, how are you?",
                LocalDateTime.now()

        );
        Conversation conversation = new Conversation(new ArrayList<>());
        conversation.addMessage(message);
        assertTrue(conversation.messages().contains(message));
    }

}
