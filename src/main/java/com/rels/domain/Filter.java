package com.rels.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents search criteria for finding properties.
 * Does not map directly to a database table.
 */
public class Filter {

    private String location; // City or Postal Code
    private String propertyType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minBedrooms;
    private Integer minBathrooms;
    private String keywords; // For searching description
    private Boolean mustBeActive; // Use Boolean wrapper to allow null (meaning "don't care")

    // Default constructor
    public Filter() {
    }

    // --- Getters and Setters ---
    // (Generate standard getters and setters for all fields)

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public Integer getMinBedrooms() { return minBedrooms; }
    public void setMinBedrooms(Integer minBedrooms) { this.minBedrooms = minBedrooms; }
    public Integer getMinBathrooms() { return minBathrooms; }
    public void setMinBathrooms(Integer minBathrooms) { this.minBathrooms = minBathrooms; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public Boolean getMustBeActive() { return mustBeActive; }
    public void setMustBeActive(Boolean mustBeActive) { this.mustBeActive = mustBeActive; }


    // --- toString (equals/hashCode might not be needed unless used in Sets/Maps) ---

    @Override
    public String toString() {
        return "Filter{" +
                "location='" + location + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", minBedrooms=" + minBedrooms +
                ", minBathrooms=" + minBathrooms +
                ", keywords='" + keywords + '\'' +
                ", mustBeActive=" + mustBeActive +
                '}';
    }
}