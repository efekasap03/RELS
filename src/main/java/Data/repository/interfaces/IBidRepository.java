package Data.repository.interfaces;

import Data.domain.Bid;
import java.util.List;

public interface IBidRepository {
    void save(Bid bid);
    boolean update(Bid bid);
    Bid findById(String bidId);
    List<Bid> findByPropertyId(String propertyId);

}
