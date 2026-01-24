package com.aspd.backend.model;

public enum OepnvStatus {
    FOUR_A,
    FOUR_B,
    NA;

    public boolean isAvailable() {
        return this == FOUR_A || this == FOUR_B;
    }
}
