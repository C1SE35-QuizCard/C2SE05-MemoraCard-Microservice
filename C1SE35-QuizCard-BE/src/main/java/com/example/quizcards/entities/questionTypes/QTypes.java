package com.example.quizcards.entities.questionTypes;

public enum QTypes {
    MULTIPLE("Multiple"),
    ESSAY("Essay");

    private final String type;

    QTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
