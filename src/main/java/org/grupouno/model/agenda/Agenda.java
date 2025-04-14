package org.grupouno.model.agenda;

import java.util.HashMap;
import java.util.List;

/**
 * Class that represents the agenda of the user.
 */
public record Agenda(HashMap<String, User> contacts) implements IAgenda {

    @Override
    public void addContact(User contact) {
        this.contacts.put(contact.nickname(), contact);
    }

    @Override
    public boolean isContactInAgenda(String contactNickname) {
        return this.contacts.containsKey(contactNickname);
    }

    @Override
    public User getContactByNickname(String contactNickname) {
        return this.contacts.get(contactNickname);
    }

    @Override
    public List<String> getContactNicknames() {
        return this.contacts.keySet().stream().toList();
    }
}
