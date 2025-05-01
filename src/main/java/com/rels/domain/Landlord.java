package com.rels.domain;

import java.util.Objects;

/**
 * Represents a Landlord user, specializing the base User class.
 */
public class Landlord extends User {

    private String agentLicenseNumber;


    public Landlord() {
        super();
        super.setRole("LANDLORD");
    }

    public Landlord(String userId, String name, String email, String passwordHash, String agentLicenseNumber) {
        super(userId, name, email, passwordHash, "LANDLORD");
        this.agentLicenseNumber = agentLicenseNumber;
    }

    // --- Getter and Setter for specific field ---

    public String getAgentLicenseNumber() {
        return agentLicenseNumber;
    }

    public void setAgentLicenseNumber(String agentLicenseNumber) {
        this.agentLicenseNumber = agentLicenseNumber;
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false; // Check base class equality first
        Landlord landlord = (Landlord) o;
        return Objects.equals(agentLicenseNumber, landlord.agentLicenseNumber);
    }

    @Override
    public int hashCode() {
        // Combine base class hash with specific field hash
        return Objects.hash(super.hashCode(), agentLicenseNumber);
    }

    @Override
    public String toString() {
        return "Landlord{" +
                "user=" + super.toString() + // Include base User info
                ", agentLicenseNumber='" + agentLicenseNumber + '\'' +
                '}';
    }

//    @Override
//    public String getUserId() {
//        return super.getUserId();  // veya kendi userId alanın
//    }
//
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public void setPasswordHash(String passwordHash) {
//        this.passwordHash = passwordHash;
//    }
}