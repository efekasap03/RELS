package UserOperations;

import java.time.LocalDateTime;
import java.util.*;
import com.rels.domain.Bid;
import java.math.BigDecimal;

public class BidManagement implements IBidManagement {
    private final Map<String, Bid> bids = new HashMap<>();

    @Override
    public String createBid(String propertyId, String clientId, double amount) {
        Bid bid = new Bid();
        String bidId = UUID.randomUUID().toString();

        bid.setBidId(bidId);
        bid.setPropertyId(propertyId);
        bid.setClientId(clientId);
        bid.setAmount(BigDecimal.valueOf(amount));
        bid.setStatus("PENDING");
        bid.setBidTimestamp(LocalDateTime.now());
        bid.setCreatedAt(LocalDateTime.now());
        bid.setUpdatedAt(LocalDateTime.now());

        bids.put(bidId, bid);
        return bidId;

    }

    @Override
    public boolean updateBid(String bidId, double newAmount) {
        Bid bid = bids.get(bidId);
        if (bid == null || !"PENDING".equalsIgnoreCase(bid.getStatus())) return false;
        bid.setAmount(BigDecimal.valueOf(newAmount));
        bid.setUpdatedAt(LocalDateTime.now());
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
}
