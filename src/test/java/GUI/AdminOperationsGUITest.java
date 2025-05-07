package GUI; // Ensure this matches the package of your AdminOperationsGUI class

import UserOperations.IBidManagement;
import UserOperations.IPropertyManagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // Import Mockito methods like mock()

import javax.swing.WindowConstants;


class AdminOperationsGUITest {

    // Mock dependencies required by the constructor signature,
    // although the class internally creates its *own* dependencies.
    private IBidManagement mockBidService;
    private IPropertyManagement mockPropertyService;

    @BeforeEach
    void setUp() {
        // Initialize Mockito mocks before each test
        mockBidService = mock(IBidManagement.class);
        mockPropertyService = mock(IPropertyManagement.class);
    }

    @Test
    void testAdminOperationsGUIConstructorSetsBasicJFrameProperties() {
        // Arrange: We have the mock services set up in @BeforeEach.

        // Act: Create an instance of the AdminOperationsGUI class.
        // The constructor will run initComponents() which sets JFrame properties.
        AdminOperationsGUI frame = null;
        try {
            // Instantiating this WILL attempt to connect to the database.
            // If it fails, an exception will be thrown here.
            frame = new AdminOperationsGUI(mockBidService, mockPropertyService);

            // Assert: Check if basic JFrame properties were set correctly by the constructor/initComponents.
            assertNotNull(frame, "JFrame instance should not be null");
            assertEquals("Admin Operations", frame.getTitle(), "JFrame title should be 'Admin Operations'");
            // Note: Checking exact size might sometimes be flaky depending on OS/L&F,
            // but for a simple fixed size it's often acceptable for basic checks.
            assertEquals(700, frame.getWidth(), "JFrame width should be 700");
            assertEquals(500, frame.getHeight(), "JFrame height should be 500");
            // Note: AdminOperationsGUI uses DISPOSE_ON_CLOSE
            assertEquals(WindowConstants.DISPOSE_ON_CLOSE, frame.getDefaultCloseOperation(), "JFrame default close operation should be DISPOSE_ON_CLOSE");

            // We cannot reliably test the private fields (adminService, propertyService),
            // the presence/setup of GUI components (tabs, panels, buttons, text areas),
            // or the logic within any of the private helper methods.

        } catch (Exception e) {
            // If the constructor failed (most likely due to DatabaseConnectorImpl issues
            // inside the constructor), the test itself failed to even create the object for checking.
            fail("Failed to instantiate AdminOperationsGUI. This is highly likely due to the class's tight coupling to a concrete DatabaseConnectorImpl created directly in the constructor," +
                    " which requires a running database instance. The class cannot be properly unit tested in isolation without refactoring.", e);
        } finally {
            // Clean up the JFrame instance if it was created.
            if (frame != null) {
                frame.dispose();
            }
        }
    }

}