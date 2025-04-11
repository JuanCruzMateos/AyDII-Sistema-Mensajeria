package org.grupouno.model;

import java.util.HashMap;

/**
 * Class that represents the agenda of the user.
 * It contains the owner of the agenda and the contacts.
 */
public record Agenda(HashMap<String, User> contacts) {

    public void addContact(User contact) {
        this.contacts.put(contact.nickname(), contact);
    }

    public boolean isContactInAgenda(String contactNickname) {
        return this.contacts.containsKey(contactNickname);
    }

    public User getContactByNickname(String contactNickname) {
        return this.contacts.get(contactNickname);
    }
}
