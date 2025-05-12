package com.example.quizcards.entities.plans;

public enum PlansName {
    FREE_PLAN("Free Plan"),
    PREMIUM_PLAN("Premium Plan");

    private final String message;

    // Constructor to set the message for each enum constant
    PlansName(String message) {
        this.message = message;
    }

    // Getter method to retrieve the message
    public String getMessage() {
        return this.message;
    }
}
