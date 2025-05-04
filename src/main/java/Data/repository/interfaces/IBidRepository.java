package Data.repository.interfaces;

import Data.domain.Bid;

import java.util.List;

public interface IBidRepository {
    boolean addBid(Bid bid);
    Bid getBidById(String bidId);
    boolean updateBid(Bid bid);
    List<Bid> getBidsByPropertyId(String propertyId);
    List<Bid> getBidsByUserId(String userId);
}
