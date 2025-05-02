package UserOperations;

import java.time.LocalDateTime;
import java.util.*;
import com.rels.domain.Bid;
import java.math.BigDecimal;
import com.rels.repository.interfaces.IBidRepository;
import java.util.List;
import java.util.UUID;

public class BidManagement implements IBidManagement {
    private final IBidRepository bidRepo;

    public BidManagement(IBidRepository bidRepo) {
        this.bidRepo = bidRepo;
    }
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

        bidRepo.save(bid);
        return bidId;

    }

    @Override
    public boolean updateBid(String bidId, double newAmount) {
        Bid bid = bidRepo.findById(bidId);
        if (bid == null || !"PENDING".equalsIgnoreCase(bid.getStatus())) return false;
        bid.setAmount(BigDecimal.valueOf(newAmount));
        bid.setUpdatedAt(LocalDateTime.now());
        return bidRepo.update(bid);
    }

    @Override
    public String getBidStatus(String bidId) {
        Bid bid = bidRepo.findById(bidId);
        return bid != null ? bid.getStatus() : null;
    }

    @Override
    public List<String> listBidsByProperty(String propertyId) {
        List<Bid> bids = bidRepo.findByPropertyId(propertyId);
        List<String> results = new ArrayList<>();
        for (Bid b : bids) {
            results.add(b.toString());
        }
        return results;
    }
}
