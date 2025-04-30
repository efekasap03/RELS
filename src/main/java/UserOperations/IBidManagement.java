package UserOperations;

import java.util.List;

public interface IBidManagement {
    String createBid(String propertyId, String clientId, double amount);
    boolean updateBid(String bidId, double newAmount);
    String getBidStatus(String bidId);
    List<String> listBidsByProperty(String propertyId);
}
