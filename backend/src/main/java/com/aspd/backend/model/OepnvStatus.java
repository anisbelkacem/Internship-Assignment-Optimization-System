package com.aspd.backend.model;

public enum OepnvStatus {
    FOUR_A("4a"),
    FOUR_B("4b"),
    NONE("none");

    private final String label;

    OepnvStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAvailable() {
        return this == FOUR_A || this == FOUR_B;
    }
}
