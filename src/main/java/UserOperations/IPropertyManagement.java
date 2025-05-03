package UserOperations;
import java.util.List;
import com.rels.domain.Property;

public interface IPropertyManagement {
    void addProperty(Property property);
    void editProperty(Property property,String landlordId);
    void deactivateProperty(String propertyId,String landlordID);
    List<Property> getProperties();
    List<Property> getActiveProperties();
    List<Property> getPropertiesByLandlord(String landlordId);
    public List<Property> searchProperties(String type, Double minPrice, Double maxPrice, String location);

}
