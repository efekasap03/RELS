package UserOperations;

import com.rels.domain.Landlord;
import com.rels.domain.Property;
import com.rels.domain.Bid;
import java.util.List;

public interface IAdminOperations {
    boolean addLandlord(com.rels.domain.Landlord landlord);
    boolean editLandlord(com.rels.domain.Landlord landlord);
    List<com.rels.domain.Property> monitorProperties();
    List<com.rels.domain.Bid> monitorBids();
    String generateReports();
    public List<Landlord> getAllLandlords();

}

