package com.rels.domain;

import java.util.Objects;

/**
 * Represents a Client user, specializing the base User class.
 */
public class Client extends User {

    private boolean receivesMarketUpdates;

    public Client() {
        super(); 
        super.setRole("CLIENT"); 
    }

    public Client(String userId, String name, String email, String passwordHash, boolean receivesMarketUpdates) {
        super(userId, name, email, passwordHash, "CLIENT");
        this.receivesMarketUpdates = receivesMarketUpdates;
    }

    // --- Getter and Setter for specific field ---

    public boolean isReceivesMarketUpdates() {
        return receivesMarketUpdates;
    }

    public void setReceivesMarketUpdates(boolean receivesMarketUpdates) {
        this.receivesMarketUpdates = receivesMarketUpdates;
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false; // Check base class equality first
        Client client = (Client) o;
        return receivesMarketUpdates == client.receivesMarketUpdates;
    }

    @Override
    public int hashCode() {
        // Combine base class hash with specific field hash
        return Objects.hash(super.hashCode(), receivesMarketUpdates);
    }

    @Override
    public String toString() {
        return "Client{" +
                "user=" + super.toString() + // Include base User info
                ", receivesMarketUpdates=" + receivesMarketUpdates +
                '}';
    }
}