package UserOperations;

import Data.domain.Landlord;
import Data.domain.Property;
import Data.domain.Bid;
import java.util.List;

public interface IAdminOperations {
    boolean addLandlord(Landlord landlord);
    boolean editLandlord(Landlord landlord);
    List<Property> monitorProperties();
    List<Bid> monitorBids();
    String generateReports();
    public List<Landlord> getAllLandlords();

}