package Data.repository.interfaces;

import Data.domain.Filter;
import Data.domain.Property;

import java.util.List;

public interface IPropertyRepository {
    boolean addProperty(Property property);
    Property getPropertyById(String propertyId);
    boolean updateProperty(Property property);
    boolean deactivateProperty(String propertyId);
    List<Property> findProperties(Filter filter);
}