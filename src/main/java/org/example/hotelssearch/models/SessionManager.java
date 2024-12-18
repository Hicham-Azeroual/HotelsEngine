package org.example.hotelssearch.models;

public class SessionManager {

    private static User currentUser;

    // Méthode pour définir l'utilisateur actuel
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Méthode pour récupérer l'utilisateur actuel
    public static User getCurrentUser() {
        return currentUser;
    }

    // Méthode pour déconnecter l'utilisateur
    public static void logout() {
        currentUser = null;
    }
}
