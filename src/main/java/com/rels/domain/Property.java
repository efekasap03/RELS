package com.rels.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a Property listing in the system.
 */
public class Property {

    private String propertyId;
    private String landlordId; // Foreign Key to User (Landlord)
    private String address;
    private String city;
    private String postalCode;
    private String propertyType; // e.g., 'Apartment', 'House', 'Condo'
    private String description;
    private BigDecimal price; // Use BigDecimal for currency/precision
    private BigDecimal squareFootage;
    private Integer bedrooms; // Use Integer wrapper to allow null
    private Integer bathrooms; // Use Integer wrapper to allow null
    private boolean isActive;
    private LocalDateTime dateListed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Property() {
    }

    // --- Getters and Setters ---
    // (Generate standard getters and setters for all fields)


    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
    public String getLandlordId() { return landlordId; }
    public void setLandlordId(String landlordId) { this.landlordId = landlordId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getSquareFootage() { return squareFootage; }
    public void setSquareFootage(BigDecimal squareFootage) { this.squareFootage = squareFootage; }
    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }
    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDateTime getDateListed() { return dateListed; }
    public void setDateListed(LocalDateTime dateListed) { this.dateListed = dateListed; }
     public void setDateListed(java.sql.Timestamp timestamp) {
        this.dateListed = (timestamp != null) ? timestamp.toLocalDateTime() : null;
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
     public void setCreatedAt(java.sql.Timestamp timestamp) {
        this.createdAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;
    }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
     public void setUpdatedAt(java.sql.Timestamp timestamp) {
        this.updatedAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;
    }


    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return Objects.equals(propertyId, property.propertyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyId);
    }

    @Override
    public String toString() {
        return "Property:" + '\n' +
                "PropertyId = " + propertyId + '\n' +
                "LandlordId = " + landlordId + '\n' +
                "Address = " + address + '\n' +
                "City = " + city + '\n' +
                "Price = " + price +'\n' +
                "isActive = " + isActive ;
    }
}