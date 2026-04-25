package org.example.provider;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.db.FriendRequestDao;
import org.example.model.FriendRequest;

import java.util.List;

public class FriendRequestProvider {
    private static FriendRequestProvider instance;
    private final ObservableList<FriendRequest> pendingRequests = FXCollections.observableArrayList();

    private FriendRequestProvider() {
        loadRequests();
    }

    public static FriendRequestProvider getInstance() {
        if (instance == null) {
            instance = new FriendRequestProvider();
        }
        return instance;
    }

    private String getCurrentUserId() {
        return AuthProvider.getInstance().getCurrentUser() != null
            ? AuthProvider.getInstance().getCurrentUser().getId()
            : "unknown";
    }

    public void loadRequests() {
        String userId = getCurrentUserId();
        if (!"unknown".equals(userId)) {
            List<FriendRequest> requests = FriendRequestDao.getPendingRequestsForUser(userId);
            pendingRequests.setAll(requests);
        }
    }

    public ObservableList<FriendRequest> getPendingRequests() {
        return pendingRequests;
    }

    public void sendRequest(String receiverId) {
        FriendRequestDao.sendRequest(getCurrentUserId(), receiverId);
    }

    public void acceptRequest(String requestId, String senderId) {
        FriendRequestDao.acceptRequest(requestId, senderId, getCurrentUserId());
        loadRequests();
        // Recargar los contactos para que el nuevo amigo aparezca en la lista
        ContactProvider.getInstance().loadContacts();
    }

    public void rejectRequest(String requestId) {
        FriendRequestDao.rejectRequest(requestId);
        loadRequests();
    }
}
