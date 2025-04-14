package org.grupouno.model.agenda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class UserTest {
    @Test
    void userRecordStoresCorrectNickname() {
        User user = new User("john_doe", "192.168.1.1", 8080);
        assertEquals("john_doe", user.nickname());
    }

    @Test
    void userRecordStoresCorrectIpAddress() {
        User user = new User("john_doe", "192.168.1.1", 8080);
        assertEquals("192.168.1.1", user.ip());
    }

    @Test
    void userRecordStoresCorrectPort() {
        User user = new User("john_doe", "192.168.1.1", 8080);
        assertEquals(8080, user.port());
    }

    @Test
    void userRecordHandlesEmptyNickname() {
        User user = new User("", "192.168.1.1", 8080);
        assertEquals("", user.nickname());
    }

    @Test
    void userRecordHandlesNullIpAddress() {
        User user = new User("john_doe", null, 8080);
        assertNull(user.ip());
    }

    @Test
    void userRecordHandlesNegativePort() {
        User user = new User("john_doe", "192.168.1.1", -1);
        assertEquals(-1, user.port());
    }
}
