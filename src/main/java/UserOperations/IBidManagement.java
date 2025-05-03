package UserOperations;

import com.rels.domain.Bid;

import java.util.List;

public interface IBidManagement {
    String createBid(String propertyId, String clientId, double amount);
    boolean updateBid(String bidId, double newAmount);
    String getBidStatus(String bidId);
    List<String> listBidsByProperty(String propertyId);
    List<String> listBidsByClient(String clientId);
    List<Bid> getBidsByLandlord(String landlordId);
    boolean updateBidStatus(String bidId, String newStatus, String landlordId);
}