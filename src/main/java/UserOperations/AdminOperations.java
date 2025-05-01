package UserOperations;

import com.rels.domain.Landlord;
import com.rels.domain.Property;
import com.rels.domain.Bid;
import com.rels.connector.DatabaseConnectorImpl;

import java.util.ArrayList;
import java.util.List;

public class AdminOperations implements UserOperations.IAdminOperations {

    private final DatabaseConnectorImpl dbManager;

    public AdminOperations(DatabaseConnectorImpl dbManager) {
        this.dbManager = dbManager;
    }
    //Issue1: replace these part with real db interaction
    private final List<Landlord> landlords = new ArrayList<>();
    private final List<Property> properties = new ArrayList<>();
    private final List<Bid> bids = new ArrayList<>();

    @Override
    public boolean addLandlord(Landlord landlord) {
        // ISSUE-2: Implement JDBC INSERT into users table
        return landlords.add(landlord);
    }

    @Override
    public boolean editLandlord(Landlord landlord) {
        // ISSUE-2: Implement JDBC UPDATE on users table
        for (int i = 0; i < landlords.size(); i++) {
            if (landlords.get(i).getUserId().equals(landlord.getUserId())) {
                landlords.set(i, landlord);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Property> monitorProperties() {
        // ISSUE-3: Implement JDBC SELECT * FROM properties WHERE is_active=TRUE
        return new ArrayList<>(properties);
    }

    @Override
    public List<Bid> monitorBids() {
        // ISSUE-3: Implement JDBC SELECT * FROM bids
        return new ArrayList<>(bids);
    }

    @Override
    public String generateReports() {
        return "";
    }

    // Issue 4: should be added report generator part



}
