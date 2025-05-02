package UserOperations;
import java.util.List;
import com.rels.domain.Property;

public interface IPropertyManagement {
    void addProperty(Property property);
    void editProperty(Property property);
    void deactivateProperty(String propertyId);
    List<Property> getProperties();

}
