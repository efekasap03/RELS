package UserOperations;

import java.time.LocalDateTime;
import java.util.*;

public class BidManagement implements IBidManagement {
    private final Map<String, Bid> bids = new HashMap<>();

    @Override
    public String createBid(String propertyId, String clientId, double amount) {
        String bidId = UUID.randomUUID().toString();
        Bid bid = new Bid(bidId, propertyId, clientId, amount, BidStatus.PENDING, LocalDateTime.now());
        bids.put(bidId, bid);
        return bidId;
    }

    @Override
    public boolean updateBid(String bidId, double newAmount) {
        Bid bid = bids.get(bidId);
        if (bid == null || bid.getStatus() != BidStatus.PENDING) return false;
        bid.setAmount(newAmount);
        bid.setTimestamp(LocalDateTime.now());
        return true;
    }

    @Override
    public String getBidStatus(String bidId) {
        Bid bid = bids.get(bidId);
        return bid != null ? bid.getStatus().toString() : null;
    }

    @Override
    public List<String> listBidsByProperty(String propertyId) {
        List<String> results = new ArrayList<>();
        for (Bid b : bids.values()) {
            if (b.getPropertyId().equals(propertyId)) {
                results.add(b.toString());
            }
        }
        return results;
    }

    // Inner classes
    private enum BidStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        CANCELLED
    }

    private static class Bid {
        private final String id;
        private final String propertyId;
        private final String clientId;
        private double amount;
        private BidStatus status;
        private LocalDateTime timestamp;

        public Bid(String id, String propertyId, String clientId,
                   double amount, BidStatus status, LocalDateTime timestamp) {
            this.id = id;
            this.propertyId = propertyId;
            this.clientId = clientId;
            this.amount = amount;
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getPropertyId() {
            return propertyId;
        }

        public BidStatus getStatus() {
            return status;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public void setStatus(BidStatus status) {
            this.status = status;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "Bid{" +
                    "id='" + id + '\'' +
                    ", propertyId='" + propertyId + '\'' +
                    ", clientId='" + clientId + '\'' +
                    ", amount=" + amount +
                    ", status=" + status +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
