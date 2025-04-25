package org.grupouno.model.directory;

import java.util.List;
import java.util.Set;

/**
 * Interface that represents the agenda of the user.
 * <p>
 * It contains methods to add contacts, check if a contact is in the agenda,
 * get a contact by its nickname, and get all contact nicknames.
 */
public interface IDirectory {
    void addContact(User contact);

    void removeContact(String contactNickname);

    boolean isContactInAgenda(String contactNickname);

    User getContactByNickname(String contactNickname);

    List<String> getContactNicknames();

    Set<User> getContacts();
}
