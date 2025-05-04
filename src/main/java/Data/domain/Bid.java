package Data.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


public class Bid {

    private String bidId;
    private String propertyId; // Foreign Key to Property
    private String clientId; // Foreign Key to User (Client)
    private BigDecimal amount; // Use BigDecimal for currency/precision
    private String status; // e.g., 'PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'
    private LocalDateTime bidTimestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Bid() {
    }

    // --- Getters and Setters ---
    public String getBidId() { return bidId; }
    public void setBidId(String bidId) { this.bidId = bidId; }
    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getBidTimestamp() { return bidTimestamp; }
    public void setBidTimestamp(LocalDateTime bidTimestamp) { this.bidTimestamp = bidTimestamp; }
    public void setBidTimestamp(java.sql.Timestamp timestamp) {
        this.bidTimestamp = (timestamp != null) ? timestamp.toLocalDateTime() : null;
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
        Bid bid = (Bid) o;
        return Objects.equals(bidId, bid.bidId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bidId);
    }

    @Override
    public String toString() {
        return "Bid{" +
                "bidId='" + bidId + '\'' +
                ", propertyId='" + propertyId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", bidTimestamp=" + bidTimestamp +
                '}';
    }
}