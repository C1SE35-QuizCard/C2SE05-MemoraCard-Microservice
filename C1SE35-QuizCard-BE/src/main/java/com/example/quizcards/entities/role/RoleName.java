package com.example.quizcards.entities.role;

public enum RoleName {
    //    GUEST("Guest"),
    ROLE_FREE_USER("ROLE_FREE_USER"),
    ROLE_PREMIUM_USER("ROLE_PREMIUM_USER"),
    ROLE_ADMIN("ROLE_ADMIN");

    private final String message;

    // Constructor to set the message for each enum constant
    RoleName(String message) {
        this.message = message;
    }

    // Getter method to retrieve the message
    public String getMessage() {
        return this.message;
    }
}
