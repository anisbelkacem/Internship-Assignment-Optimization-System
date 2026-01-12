package com.aspd.backend.model;

public enum AuditAction {
    CREATE("Created"),
    UPDATE("Updated"),
    DELETE("Deleted");

    private final String displayName;

    AuditAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
