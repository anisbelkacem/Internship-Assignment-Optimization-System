package com.aspd.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing geographic coordinates
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesDto {
    private Double longitude;
    private Double latitude;

    /**
     * Calculate distance between this point and another in kilometers
     * Uses Haversine formula
     */
    public Double distanceTo(CoordinatesDto other) {
        if (this.longitude == null || this.latitude == null || 
            other.longitude == null || other.latitude == null) {
            return null;
        }

        final int EARTH_RADIUS_KM = 6371;

        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
