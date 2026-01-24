package com.aspd.backend.service;

import com.aspd.backend.dto.CoordinatesDto;
import com.aspd.backend.dto.GeoapifyResponse;
import com.aspd.backend.model.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Service for integrating with Geoapify API to convert addresses to coordinates
 */
@Service
@Slf4j
public class GeoapifyService {

    private final WebClient webClient;
    private final String apiKey;
    private static final String GEOAPIFY_BASE_URL = "https://api.geoapify.com";

    public GeoapifyService(WebClient.Builder webClientBuilder,
                          @Value("${geoapify.api-key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl(GEOAPIFY_BASE_URL).build();
        this.apiKey = apiKey;
    }

    /**
     * Convert an address to coordinates using Geoapify API
     *
     * @param address the Address object containing street, city, postal code, country
     * @return Optional containing CoordinatesDto if conversion is successful
     */
    public Optional<CoordinatesDto> getCoordinates(Address address) {
        if (address == null || !isValidAddress(address)) {
            log.warn("Invalid address provided for geolocation: {}", address);
            return Optional.empty();
        }

        String formattedAddress = formatAddress(address);
        return getCoordinatesByAddressString(formattedAddress);
    }

    /**
     * Convert an address string to coordinates using Geoapify API
     *
     * @param addressString the formatted address string
     * @return Optional containing CoordinatesDto if conversion is successful
     */
    public Optional<CoordinatesDto> getCoordinatesByAddressString(String addressString) {
        if (addressString == null || addressString.trim().isEmpty()) {
            log.warn("Empty address string provided for geolocation");
            return Optional.empty();
        }

        try {
            GeoapifyResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/geocode/search")
                            .queryParam("text", addressString)
                            .queryParam("apiKey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(GeoapifyResponse.class)
                    .block();

            if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
                GeoapifyResponse.Feature feature = response.getFeatures().get(0);
                if (feature.getGeometry() != null && feature.getGeometry().getCoordinates().length >= 2) {
                    double[] coords = feature.getGeometry().getCoordinates();
                    return Optional.of(new CoordinatesDto(coords[0], coords[1])); // [lon, lat]
                }
            }

            log.warn("No results found for address: {}", addressString);
            return Optional.empty();

        } catch (WebClientResponseException e) {
            log.error("Geoapify API error for address '{}': {} - {}", addressString, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error calling Geoapify API for address '{}'", addressString, e);
            return Optional.empty();
        }
    }

    /**
     * Validate that address has at least minimum required fields
     */
    private boolean isValidAddress(Address address) {
        // Address is valid if it has at least one meaningful field
        return (address.getStreet() != null && !address.getStreet().trim().isEmpty()) ||
               (address.getCity() != null && !address.getCity().trim().isEmpty()) ||
               (address.getPostalCode() != null && !address.getPostalCode().trim().isEmpty()) ||
               (address.getCountry() != null && !address.getCountry().trim().isEmpty());
    }

    /**
     * Format address components into a single string for API query
     */
    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();

        if (address.getStreet() != null && !address.getStreet().isEmpty()) {
            sb.append(address.getStreet()).append(" ");
        }
        if (address.getHouseNbr() != null && !address.getHouseNbr().isEmpty()) {
            sb.append(address.getHouseNbr()).append(" ");
        }
        if (address.getPostalCode() != null && !address.getPostalCode().isEmpty()) {
            sb.append(address.getPostalCode()).append(" ");
        }
        if (address.getCity() != null && !address.getCity().isEmpty()) {
            sb.append(address.getCity()).append(" ");
        }
        if (address.getCountry() != null && !address.getCountry().isEmpty()) {
            sb.append(address.getCountry());
        }

        return sb.toString().trim();
    }

    /**
     * Calculate distance between two addresses in kilometers
     *
     * @param address1 first address
     * @param address2 second address
     * @return Optional containing distance in km if both addresses can be geocoded
     */
    public Optional<Double> calculateDistance(Address address1, Address address2) {
        Optional<CoordinatesDto> coords1 = getCoordinates(address1);
        Optional<CoordinatesDto> coords2 = getCoordinates(address2);

        if (coords1.isPresent() && coords2.isPresent()) {
            return Optional.of(coords1.get().distanceTo(coords2.get()));
        }

        return Optional.empty();
    }

    /**
     * Calculate distance between two coordinate sets in kilometers
     *
     * @param coords1 first set of coordinates
     * @param coords2 second set of coordinates
     * @return distance in kilometers
     */
    public Double calculateDistance(CoordinatesDto coords1, CoordinatesDto coords2) {
        if (coords1 == null || coords2 == null) {
            return null;
        }
        return coords1.distanceTo(coords2);
    }
}
