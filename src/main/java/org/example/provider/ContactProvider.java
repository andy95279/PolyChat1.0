package org.example.provider;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.db.ContactDao;
import org.example.model.Contact;

import java.util.List;

public class ContactProvider {
    private static ContactProvider instance;
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();

    private ContactProvider() {
        loadContacts();
    }

    private String getCurrentUserId() {
        return AuthProvider.getInstance().getCurrentUser() != null
            ? AuthProvider.getInstance().getCurrentUser().getId()
            : "unknown";
    }

    public static ContactProvider getInstance() {
        if (instance == null)
            instance = new ContactProvider();
        return instance;
    }

    public void loadContacts() {
        List<Contact> dbContacts = ContactDao.getContactsForUser(getCurrentUserId());
        contacts.setAll(dbContacts);
    }

    public ObservableList<Contact> getContacts() {
        return contacts;
    }

    public List<Contact> searchUsers(String query) {
        return ContactDao.searchUsers(query, getCurrentUserId());
    }

    public void addContact(String contactId) {
        ContactDao.addContact(getCurrentUserId(), contactId);
        loadContacts();
    }
}
