package org.grupouno.model.directory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Class that represents the agenda of the user.
 */
public class Directory implements IDirectory, Serializable {
    private final HashMap<String, User> contacts;

    public Directory() {
        this.contacts = new HashMap<>();
    }

    @Override
    public synchronized void addContact(User contact) {
        this.contacts.put(contact.nickname(), contact);
    }

    @Override
    public synchronized void removeContact(String contactNickname) {
        this.contacts.remove(contactNickname);
    }

    @Override
    public synchronized boolean isContactInAgenda(String contactNickname) {
        return this.contacts.containsKey(contactNickname);
    }

    @Override
    public synchronized User getContactByNickname(String contactNickname) {
        return this.contacts.get(contactNickname);
    }

    @Override
    public synchronized List<String> getContactNicknames() {
        return this.contacts.keySet().stream().toList();
    }

    @Override
    public Set<User> getContacts() {
        return Set.copyOf(this.contacts.values());
    }
}
