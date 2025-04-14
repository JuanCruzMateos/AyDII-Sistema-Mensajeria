package org.grupouno.model.agenda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AgendaTest {

    private Agenda agenda;

    @BeforeEach
    void setUp() {
        agenda = new Agenda(new HashMap<>());
    }

    @Test
    void addContactAddsUserToContacts() {
        User user = new User("john_doe", "127.0.0.1", 8080);
        agenda.addContact(user);
        assertTrue(agenda.isContactInAgenda("john_doe"));
    }

    @Test
    void isContactInAgendaReturnsTrueForExistingContact() {
        User user = new User("jane_doe", "127.0.0.1", 8080);
        agenda.addContact(user);
        assertTrue(agenda.isContactInAgenda("jane_doe"));
    }

    @Test
    void isContactInAgendaReturnsFalseForNonExistingContact() {
        assertFalse(agenda.isContactInAgenda("non_existent"));
    }

    @Test
    void getContactByNicknameReturnsCorrectUser() {
        User user = new User("john_doe", "127.0.0.1", 8080);
        agenda.addContact(user);
        assertEquals(user, agenda.getContactByNickname("john_doe"));
    }

    @Test
    void getContactByNicknameReturnsNullForNonExistingContact() {
        assertNull(agenda.getContactByNickname("non_existent"));
    }

    @Test
    void getContactNicknamesReturnsAllNicknames() {
        User user1 = new User("john_doe", "127.0.0.1", 8080);
        User user2 = new User("john_john", "127.0.0.2", 8080);
        agenda.addContact(user1);
        agenda.addContact(user2);
        List<String> nicknames = agenda.getContactNicknames();
        assertTrue(nicknames.contains("john_doe"));
        assertTrue(nicknames.contains("john_john"));
        assertEquals(2, nicknames.size());
    }

    @Test
    void addContactOverwritesExistingContactWithSameNickname() {
        User user1 = new User("john_doe", "127.0.0.1", 8080);
        User user2 = new User("john_doe", "127.0.0.2", 5008);
        agenda.addContact(user1);
        agenda.addContact(user2);
        assertEquals(user2, agenda.getContactByNickname("john_doe"));
    }
}