package GUI; // Ensure this matches the package of your PropertyManagementGUI class

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.WindowConstants;

class PropertyManagementGUITest {
    @Test
    void testPropertyManagementGUIConstructorSetsBasicJFrameProperties() {
        // Arrange: Define the landlord ID to pass to the constructor.
        String testLandlordId = "testUser123";

        // Act: Create an instance of the PropertyManagementGUI class.
        // The constructor itself performs the actions we want to check (setting properties).
        PropertyManagementGUI frame = null;
        try {
            // The constructor might throw an exception if DatabaseConnectorImpl fails
            frame = new PropertyManagementGUI(testLandlordId);

            // Assert: Check if basic JFrame properties were set correctly by the constructor.
            assertNotNull(frame, "JFrame instance should not be null");
            assertEquals("Property Management", frame.getTitle(), "JFrame title should be 'Property Management'");
            // Note: Checking exact size might sometimes be flaky depending on OS/L&F,
            // but for a simple fixed size it's often acceptable for basic checks.
            // We check both width and height as set by setSize.
            assertEquals(700, frame.getWidth(), "JFrame width should be 700");
            assertEquals(500, frame.getHeight(), "JFrame height should be 500");
            assertEquals(WindowConstants.EXIT_ON_CLOSE, frame.getDefaultCloseOperation(), "JFrame default close operation should be EXIT_ON_CLOSE");

            // We cannot reliably test the private fields (propertyService, bidservice, landlordId)
            // or the presence/setup of GUI components and listeners
            // without changing the class or using brittle reflection (which is generally avoided in unit tests).

        } catch (Exception e) {
            // If the constructor failed (most likely due to DatabaseConnectorImpl issues),
            // the test itself failed to even create the object for checking.
            fail("Failed to instantiate PropertyManagementGUI. This is likely due to database connection issues specified in the original class." +
                    " The class's tight coupling to the database makes it hard to unit test without refactoring.", e);
        } finally {
            // Clean up the JFrame instance if it was created. This is good practice
            // to release native resources, though unit tests often run headless.
            if (frame != null) {
                frame.dispose();
            }
        }
    }

}