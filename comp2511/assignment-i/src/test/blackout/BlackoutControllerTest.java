package blackout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import unsw.blackout.BlackoutController;
import unsw.blackout.FileTransferException.VirtualFileNotFoundException;
import unsw.blackout.FileTransferException.VirtualFileAlreadyExistsException;
import unsw.blackout.FileTransferException.VirtualFileNoBandwidthException;
import unsw.blackout.FileTransferException.VirtualFileNoStorageSpaceException;
import unsw.response.models.EntityInfoResponse;
import unsw.response.models.FileInfoResponse;
import unsw.utils.Angle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static blackout.TestHelpers.assertListAreEqualIgnoringOrder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

@TestInstance(value = Lifecycle.PER_CLASS)
public class BlackoutControllerTest {

    // Testing helper functions
    public double radiansToDegrees(double radians) {
        return radians * (180 / Math.PI);
    }

    public double degreesToRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }

    // Create and Remove Devices and Satellites
    @Test
    public void testDeviceEntityInfoResponse() {
        EntityInfoResponse response = new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), 500000,
                "HandheldDevice");

        assertEquals("DeviceA", response.getDeviceId());
        assertEquals(Angle.fromDegrees(30), response.getPosition());
        assertEquals(500000, response.getHeight());
        assertEquals("HandheldDevice", response.getType());
    }

    @Test
    public void testSatelliteEntityInfoResponse() {
        EntityInfoResponse response = new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30), 500000,
                "StandardSatellite");

        assertEquals("SatelliteA", response.getDeviceId());
        assertEquals(Angle.fromDegrees(30), response.getPosition());
        assertEquals(500000, response.getHeight());
        assertEquals("StandardSatellite", response.getType());
    }

    @Test
    public void testCreateListGetDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(180));
        controller.createDevice("DeviceC", "DesktopDevice", Angle.fromDegrees(330));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB", "DeviceC"), controller.listDeviceIds());
        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceA"));
        assertEquals(new EntityInfoResponse("DeviceB", Angle.fromDegrees(180), RADIUS_OF_JUPITER, "LaptopDevice"),
                controller.getInfo("DeviceB"));
        assertEquals(new EntityInfoResponse("DeviceC", Angle.fromDegrees(330), RADIUS_OF_JUPITER, "DesktopDevice"),
                controller.getInfo("DeviceC"));

        assertNotEquals(controller.getInfo("DeviceA"), controller.getInfo("DeviceB"));
        assertNotEquals(controller.getInfo("DeviceA"), controller.getInfo("DeviceC"));
        assertNotEquals(controller.getInfo("DeviceB"), controller.getInfo("DeviceC"));
    }

    @Test
    public void testCreateListGetSatellite() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteB", "TeleportingSatellite", 150000, Angle.fromDegrees(180));
        controller.createSatellite("SatelliteC", "RelaySatellite", 200000, Angle.fromDegrees(330));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "SatelliteC"),
                controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteA"));
        assertEquals(new EntityInfoResponse("SatelliteB", Angle.fromDegrees(180), 150000, "TeleportingSatellite"),
                controller.getInfo("SatelliteB"));
        assertEquals(new EntityInfoResponse("SatelliteC", Angle.fromDegrees(330), 200000, "RelaySatellite"),
                controller.getInfo("SatelliteC"));

        assertNotEquals(controller.getInfo("SatelliteA"), controller.getInfo("SatelliteB"));
        assertNotEquals(controller.getInfo("SatelliteA"), controller.getInfo("SatelliteC"));
        assertNotEquals(controller.getInfo("SatelliteB"), controller.getInfo("SatelliteC"));
    }

    @Test
    public void testCreateRemoveDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "LaptopDevice", Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());

        controller.removeDevice("DeviceA");

        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.listDeviceIds());
    }

    @Test
    public void testCreateRemoveSatellite() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        controller.removeSatellite("SatelliteA");

        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.listSatelliteIds());
    }

    // Add files to devices

    @Test
    public void testAddFileToDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());

        controller.addFileToDevice("DeviceA", "file1", "hello");

        Map<String, FileInfoResponse> expectedA = new HashMap<>();
        expectedA.put("file1", new FileInfoResponse("file1", "hello", "hello".length(), true));

        assertEquals(expectedA, controller.getInfo("DeviceA").getFiles()); // should have 1 file

        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice",
                expectedA), controller.getInfo("DeviceA"));
    }

    @Test
    public void testAddFilesToDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(180));
        controller.createDevice("DeviceC", "DesktopDevice", Angle.fromDegrees(330));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB", "DeviceC"), controller.listDeviceIds());

        controller.addFileToDevice("DeviceA", "file1", "hello");
        controller.addFileToDevice("DeviceA", "file2", "hi");
        controller.addFileToDevice("DeviceB", "file3", "oof");

        Map<String, FileInfoResponse> expectedA = new HashMap<>();
        Map<String, FileInfoResponse> expectedB = new HashMap<>();
        Map<String, FileInfoResponse> expectedC = new HashMap<>();
        expectedA.put("file1", new FileInfoResponse("file1", "hello", "hello".length(), true));
        expectedA.put("file2", new FileInfoResponse("file2", "hi", "hi".length(), true));
        expectedB.put("file3", new FileInfoResponse("file3", "oof", "oof".length(), true));

        assertEquals(expectedA, controller.getInfo("DeviceA").getFiles()); // should have 2 files
        assertEquals(expectedB, controller.getInfo("DeviceB").getFiles()); // should have 1 file
        assertEquals(expectedC, controller.getInfo("DeviceC").getFiles()); // should be empty

        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice",
                expectedA), controller.getInfo("DeviceA"));
        assertEquals(
                new EntityInfoResponse("DeviceB", Angle.fromDegrees(180), RADIUS_OF_JUPITER, "LaptopDevice", expectedB),
                controller.getInfo("DeviceB"));
        assertEquals(new EntityInfoResponse("DeviceC", Angle.fromDegrees(330), RADIUS_OF_JUPITER, "DesktopDevice"),
                controller.getInfo("DeviceC"));
    }

    @Test
    public void testAddIdenticalFilesToDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(180));
        controller.createDevice("DeviceC", "DesktopDevice", Angle.fromDegrees(330));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB", "DeviceC"), controller.listDeviceIds());

        controller.addFileToDevice("DeviceA", "file1", "hello");
        controller.addFileToDevice("DeviceA", "file2", "hello");
        controller.addFileToDevice("DeviceA", "file3", "hello");

        Map<String, FileInfoResponse> expectedA = new HashMap<>();
        expectedA.put("file1", new FileInfoResponse("file1", "hello", "hello".length(), true));
        expectedA.put("file2", new FileInfoResponse("file2", "hello", "hello".length(), true));
        expectedA.put("file3", new FileInfoResponse("file3", "hello", "hello".length(), true));

        assertEquals(expectedA, controller.getInfo("DeviceA").getFiles()); // should have 3 files

        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice",
                expectedA), controller.getInfo("DeviceA"));
        assertEquals(new EntityInfoResponse("DeviceB", Angle.fromDegrees(180), RADIUS_OF_JUPITER, "LaptopDevice"),
                controller.getInfo("DeviceB"));
        assertEquals(new EntityInfoResponse("DeviceC", Angle.fromDegrees(330), RADIUS_OF_JUPITER, "DesktopDevice"),
                controller.getInfo("DeviceC"));
    }

    @Test
    public void testAddEmptyFileToDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());

        controller.addFileToDevice("DeviceA", "file1", "");

        Map<String, FileInfoResponse> expectedA = new HashMap<>();
        expectedA.put("file1", new FileInfoResponse("file1", "", "".length(), true));

        assertEquals(expectedA, controller.getInfo("DeviceA").getFiles()); // should have 1 files

        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice",
                expectedA), controller.getInfo("DeviceA"));
    }

    // Entities in range of devices and satellites

    @Test
    public void testEntitiesInRangeOfDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

    }

    @Test
    public void testMoreEntitiesInRangeOfDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteB", "StandardSatellite", 80000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteC", "StandardSatellite", 80000, Angle.fromDegrees(45));
        controller.createSatellite("SatelliteD", "TeleportingSatellite", 80000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "SatelliteC", "SatelliteD"),
                controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "SatelliteC", "SatelliteD"),
                controller.communicableEntitiesInRange("DeviceA"));

    }

    @Test
    public void testEntitiesOutOfRangeOfDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteB", "StandardSatellite", 200000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteC", "StandardSatellite", 100000, Angle.fromDegrees(210));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "SatelliteC"),
                controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

    }

    @Test
    public void testEntitiesInRangeOfSatellite() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.communicableEntitiesInRange("SatelliteA"));

    }

    @Test
    public void testMoreEntitiesInRangeOfSatellite() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "HandheldDevice", Angle.fromDegrees(60)); // angle further away
        controller.createDevice("DeviceC", "LaptopDevice", Angle.fromDegrees(30)); // different type

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteB", "StandardSatellite", 80000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteC", "StandardSatellite", 100000, Angle.fromDegrees(45));
        controller.createSatellite("SatelliteD", "TeleportingSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB", "DeviceC"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "SatelliteC", "SatelliteD"),
                controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(
                Arrays.asList("SatelliteB", "SatelliteC", "SatelliteD", "DeviceA", "DeviceB", "DeviceC"),
                controller.communicableEntitiesInRange("SatelliteA"));

    }

    @Test
    public void testEntitiesOutOfRangeOfSatellite() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "HandheldDevice", Angle.fromDegrees(210)); // other side of Jupiter

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteB", "StandardSatellite", 2000000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteC", "StandardSatellite", 100000, Angle.fromDegrees(210));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "SatelliteC"),
                controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.communicableEntitiesInRange("SatelliteA"));

    }

    @Test
    public void testDesktopCantCommunicateWithStandardSatellite() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "DesktopDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("DeviceA"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("SatelliteA"));

    }

    @Test
    public void testDevicesCantCommunicateWithEachOther() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceC", "DesktopDevice", Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB", "DeviceC"), controller.listDeviceIds());

        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("DeviceA"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("DeviceB"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("DeviceC"));

    }

    @Test
    public void testDevicesCantCommunicateWithEachOtherViaRelay() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceC", "DesktopDevice", Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB", "DeviceC"), controller.listDeviceIds());

        controller.createSatellite("SatelliteR", "RelaySatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteR"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteR"), controller.communicableEntitiesInRange("DeviceA"));
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteR"), controller.communicableEntitiesInRange("DeviceB"));
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteR"), controller.communicableEntitiesInRange("DeviceC"));

    }

    @Test
    public void testEntitiesInRangeViaRelay() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30)); // can only see the relay

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(60));
        controller.createSatellite("SatelliteR", "RelaySatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteR"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "SatelliteR"),
                controller.communicableEntitiesInRange("SatelliteA"));
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "DeviceA"),
                controller.communicableEntitiesInRange("SatelliteR"));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteR"),
                controller.communicableEntitiesInRange("DeviceA"));

    }

    @Test
    public void testMoreEntitiesInRangeViaRelay() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "HandheldDevice", Angle.fromDegrees(0));
        controller.createDevice("DeviceC", "HandheldDevice", Angle.fromDegrees(60));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(60));
        controller.createSatellite("SatelliteR", "RelaySatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB", "DeviceC"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteR"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteR", "DeviceA", "DeviceB", "DeviceC"),
                controller.communicableEntitiesInRange("SatelliteA"));
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "DeviceA", "DeviceB", "DeviceC"),
                controller.communicableEntitiesInRange("SatelliteR"));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteR"),
                controller.communicableEntitiesInRange("DeviceA"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("DeviceB"));
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceC"));

    }

    @Test
    public void testEntitiesInRangeViaTwoRelays() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30)); // can only see the standard

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteB", "TeleportingSatellite", 100000, Angle.fromDegrees(120));
        controller.createSatellite("SatelliteR", "RelaySatellite", 100000, Angle.fromDegrees(60));
        controller.createSatellite("SatelliteS", "RelaySatellite", 100000, Angle.fromDegrees(90));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "SatelliteR", "SatelliteS"),
                controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));
        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "SatelliteB", "SatelliteR", "SatelliteS"),
                controller.communicableEntitiesInRange("SatelliteA"));
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "DeviceA", "SatelliteR", "SatelliteS"),
                controller.communicableEntitiesInRange("SatelliteB"));
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "DeviceA", "SatelliteS"),
                controller.communicableEntitiesInRange("SatelliteR"));
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "SatelliteR", "DeviceA"),
                controller.communicableEntitiesInRange("SatelliteS"));
    }

    @Test
    public void testEntitiesOutOfRangeViaRelay() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30)); // can only see the relay
        controller.createDevice("DeviceB", "HandheldDevice", Angle.fromDegrees(210)); // lonely on other side of Jupiter

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(60));
        controller.createSatellite("SatelliteR", "RelaySatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteR"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteR", "DeviceA"),
                controller.communicableEntitiesInRange("SatelliteA"));
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "DeviceA"),
                controller.communicableEntitiesInRange("SatelliteR"));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteR"),
                controller.communicableEntitiesInRange("DeviceA"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("DeviceB"));

    }

    // Movement Simulation
    @Test
    public void testMoveStandard() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.025 radians

        double incr = radiansToDegrees(2500.0 / 100000.0);

        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30 - incr), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteA"));
    }

    @Test
    public void testMoveStandardLonger() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.025 radians

        double incr = radiansToDegrees(2500.0 / 100000.0);

        controller.simulate(120);

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30 - incr * 120 + 360), 100000,
                "StandardSatellite"), controller.getInfo("SatelliteA"));
    }

    @Test
    public void testMoveStandardPastZero() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(0));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(0), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.025 radians

        double incr = radiansToDegrees(2500.0 / 100000.0);

        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(360 - incr), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteA"));
    }

    @Test
    public void testMoveMultipleStandards() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));
        controller.createSatellite("SatelliteB", "StandardSatellite", 100000, Angle.fromDegrees(90));
        controller.createSatellite("SatelliteC", "StandardSatellite", 80000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB", "SatelliteC"),
                controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.025 radians
        assertEquals(new EntityInfoResponse("SatelliteB", Angle.fromDegrees(90), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteB")); // 1 min: moves 0.025 radians
        assertEquals(new EntityInfoResponse("SatelliteC", Angle.fromDegrees(30), 80000, "StandardSatellite"),
                controller.getInfo("SatelliteC")); // 1 min: moves 0.03125 radians

        double incr1 = radiansToDegrees(2500.0 / 100000.0);
        double incr2 = radiansToDegrees(2500.0 / 80000.0);

        controller.simulate(5);

        assertEquals(
                new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30 - incr1 * 5), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteA"));
        assertEquals(
                new EntityInfoResponse("SatelliteB", Angle.fromDegrees(90 - incr1 * 5), 100000, "StandardSatellite"),
                controller.getInfo("SatelliteB"));
        assertEquals(
                new EntityInfoResponse("SatelliteC", Angle.fromDegrees(30 - incr2 * 5), 80000, "StandardSatellite"),
                controller.getInfo("SatelliteC"));

    }

    @Test
    public void testMoveTeleporting() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "TeleportingSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30), 100000, "TeleportingSatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.01 radians

        double incr = radiansToDegrees(1000.0 / 100000.0);

        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30 - incr), 100000, "TeleportingSatellite"),
                controller.getInfo("SatelliteA"));
    }

    @Test
    public void testMoveTeleportingPastZero() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "TeleportingSatellite", 100000, Angle.fromDegrees(0.5));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(0.5), 100000, "TeleportingSatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.01 radians

        double incr = radiansToDegrees(1000.0 / 100000.0);

        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(0.5 - incr + 360.0), 100000,
                "TeleportingSatellite"), controller.getInfo("SatelliteA"));
    }

    @Test
    public void testMoveTeleportingHasTeleported() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "TeleportingSatellite", 100000, Angle.fromDegrees(180.5));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(180.5), 100000, "TeleportingSatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.01 radians

        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(0.0), 100000, "TeleportingSatellite"),
                controller.getInfo("SatelliteA"));

        double incr = radiansToDegrees(1000.0 / 100000.0);

        // take the positive direction
        controller.simulate();
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(incr), 100000, "TeleportingSatellite"),
                controller.getInfo("SatelliteA"));
    }

    @Test
    public void testMoveRelay() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "RelaySatellite", 100000, Angle.fromDegrees(180));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(180), 100000, "RelaySatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.02 radians

        double incr = radiansToDegrees(1500.0 / 100000.0);

        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(180 - incr), 100000, "RelaySatellite"),
                controller.getInfo("SatelliteA"));
    }

    @Test
    public void testMoveRelayOutOfBounds() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "RelaySatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30), 100000, "RelaySatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.02 radians

        double incr = radiansToDegrees(1500.0 / 100000.0);

        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(30 + incr), 100000, "RelaySatellite"),
                controller.getInfo("SatelliteA"));
    }

    @Test
    public void testMoveRelayChangeDirection() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "RelaySatellite", 100000, Angle.fromDegrees(140.5));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(140.5), 100000, "RelaySatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.02 radians

        double incr = radiansToDegrees(1500.0 / 100000.0);

        // briefly exceed boundary
        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(140.5 - incr), 100000, "RelaySatellite"),
                controller.getInfo("SatelliteA"));

        // take positive direction
        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(140.5), 100000, "RelaySatellite"),
                controller.getInfo("SatelliteA"));
    }

    @Test
    public void testMoveRelayAtThreshold() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("SatelliteA", "RelaySatellite", 100000, Angle.fromDegrees(345));

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());
        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(345), 100000, "RelaySatellite"),
                controller.getInfo("SatelliteA")); // 1 min: moves 0.02 radians

        double incr = radiansToDegrees(1500.0 / 100000.0);

        controller.simulate();

        assertEquals(new EntityInfoResponse("SatelliteA", Angle.fromDegrees(345 + incr), 100000, "RelaySatellite"),
                controller.getInfo("SatelliteA"));
    }

    @Test
    public void testNoDeviceMovement() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(180));
        controller.createDevice("DeviceC", "DesktopDevice", Angle.fromDegrees(330));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB", "DeviceC"), controller.listDeviceIds());
        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceA"));
        assertEquals(new EntityInfoResponse("DeviceB", Angle.fromDegrees(180), RADIUS_OF_JUPITER, "LaptopDevice"),
                controller.getInfo("DeviceB"));
        assertEquals(new EntityInfoResponse("DeviceC", Angle.fromDegrees(330), RADIUS_OF_JUPITER, "DesktopDevice"),
                controller.getInfo("DeviceC"));

        controller.simulate();

        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceA"));
        assertEquals(new EntityInfoResponse("DeviceB", Angle.fromDegrees(180), RADIUS_OF_JUPITER, "LaptopDevice"),
                controller.getInfo("DeviceB"));
        assertEquals(new EntityInfoResponse("DeviceC", Angle.fromDegrees(330), RADIUS_OF_JUPITER, "DesktopDevice"),
                controller.getInfo("DeviceC"));
    }

    // Moving Devices, no slope
    @Test
    public void testCreateMovingDeviceNoSlope() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30), true);

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceA")); // 1 min: moves (very slowly) 0.000715 radians

        assertTrue(controller.getDevice("DeviceA").canMove());
    }

    @Test
    public void testMoveMovingDeviceNoSlope() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30), true);

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceA"));

        assertTrue(controller.getDevice("DeviceA").canMove());

        double incr = radiansToDegrees(50.0 / (double) RADIUS_OF_JUPITER);
        controller.simulate();

        assertEquals(
                new EntityInfoResponse("DeviceA", Angle.fromDegrees(30 - incr), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceA"));
    }

    @Test
    public void testMoveMultipleDeviceNoSlope() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(60), true);
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(50), true);
        controller.createDevice("DeviceC", "DesktopDevice", Angle.fromDegrees(40), true);
        controller.createDevice("DeviceD", "HandheldDevice", Angle.fromDegrees(30), false);

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB", "DeviceC", "DeviceD"),
                controller.listDeviceIds());
        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(60), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceA"));
        assertEquals(new EntityInfoResponse("DeviceB", Angle.fromDegrees(50), RADIUS_OF_JUPITER, "LaptopDevice"),
                controller.getInfo("DeviceB"));
        assertEquals(new EntityInfoResponse("DeviceC", Angle.fromDegrees(40), RADIUS_OF_JUPITER, "DesktopDevice"),
                controller.getInfo("DeviceC"));
        assertEquals(new EntityInfoResponse("DeviceD", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceD"));

        assertTrue(controller.getDevice("DeviceA").canMove());
        assertTrue(controller.getDevice("DeviceB").canMove());
        assertTrue(controller.getDevice("DeviceC").canMove());
        assertTrue(!controller.getDevice("DeviceD").canMove());

        double incrA = radiansToDegrees(50.0 / RADIUS_OF_JUPITER);
        double incrB = radiansToDegrees(30.0 / RADIUS_OF_JUPITER);
        double incrC = radiansToDegrees(20.0 / RADIUS_OF_JUPITER);
        controller.simulate();

        assertEquals(
                new EntityInfoResponse("DeviceA", Angle.fromDegrees(60 - incrA), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceA"));
        assertEquals(
                new EntityInfoResponse("DeviceB", Angle.fromDegrees(50 - incrB), RADIUS_OF_JUPITER, "LaptopDevice"),
                controller.getInfo("DeviceB"));
        assertEquals(
                new EntityInfoResponse("DeviceC", Angle.fromDegrees(40 - incrC), RADIUS_OF_JUPITER, "DesktopDevice"),
                controller.getInfo("DeviceC"));
        assertEquals(new EntityInfoResponse("DeviceD", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceD"));
    }

    @Test
    public void testRemoveMovingDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30), true);

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertEquals(new EntityInfoResponse("DeviceA", Angle.fromDegrees(30), RADIUS_OF_JUPITER, "HandheldDevice"),
                controller.getInfo("DeviceA"));

        assertTrue(controller.getDevice("DeviceA").canMove());

        controller.removeDevice("DeviceA");

        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.listDeviceIds());
    }

    // Send Files between entities

    @Test
    public void testSendFileNotFound() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

        assertThrows(VirtualFileNotFoundException.class, () -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });
    }

    @Test
    public void testSendFileUploading() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

        controller.addFileToDevice("DeviceA", "file1", "e");

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", "e", "e".length(), true));

        assertEquals(expected, controller.getInfo("DeviceA").getFiles()); // should have 1 file

        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });

        assertThrows(VirtualFileNotFoundException.class, () -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });
    }

    @Test
    public void testSendFileNoBandwidth() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));
        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));
        // can only transfer 1 file at a time

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

        controller.addFileToDevice("DeviceA", "file1", "hello");
        controller.addFileToDevice("DeviceA", "file2", "hi");

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", "hello", "hello".length(), true));
        expected.put("file2", new FileInfoResponse("file2", "hi", "hi".length(), true));

        assertEquals(expected, controller.getInfo("DeviceA").getFiles()); // should have 2 files

        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });

        assertThrows(VirtualFileNoBandwidthException.class, () -> {
            controller.sendFile("file2", "DeviceA", "SatelliteA");
        });
    }

    @Test
    public void testSendFileAlreadyExists() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

        controller.addFileToDevice("DeviceA", "file1", "e");

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", "e", "e".length(), true));
        assertEquals(expected, controller.getInfo("DeviceA").getFiles()); // should have 1 file

        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });

        controller.simulate();

        assertEquals(expected, controller.getInfo("SatelliteA").getFiles());

        assertThrows(VirtualFileAlreadyExistsException.class, () -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });

        controller.addFileToDevice("DeviceA", "file1", "hello");

        assertThrows(VirtualFileAlreadyExistsException.class, () -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });
    }

    @Test
    public void testSendFileNoFileStorage() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

        controller.addFileToDevice("DeviceA", "file1", "e");
        controller.addFileToDevice("DeviceA", "file2", "e");
        controller.addFileToDevice("DeviceA", "file3", "e");

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", "e", "e".length(), true));
        expected.put("file2", new FileInfoResponse("file2", "e", "e".length(), true));
        expected.put("file3", new FileInfoResponse("file3", "e", "e".length(), true));
        assertEquals(expected, controller.getInfo("DeviceA").getFiles()); // should have 3 files

        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });
        controller.simulate();
        assertDoesNotThrow(() -> {
            controller.sendFile("file2", "DeviceA", "SatelliteA");
        });
        controller.simulate();
        assertDoesNotThrow(() -> {
            controller.sendFile("file3", "DeviceA", "SatelliteA");
        });
        controller.simulate();

        assertEquals(expected, controller.getInfo("SatelliteA").getFiles());

        controller.addFileToDevice("DeviceA", "file4", "hello");

        assertThrows(VirtualFileNoStorageSpaceException.class, () -> {
            controller.sendFile("file4", "DeviceA", "SatelliteA");
        });
    }

    @Test
    public void testSendFileNoByteStorage() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

        String content = "The biggest lie in the entire universe: I have read and agree to the Terms & Conditions.";

        controller.addFileToDevice("DeviceA", "file1", content);

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", content, content.length(), true));
        assertEquals(expected, controller.getInfo("DeviceA").getFiles()); // should have 3 files

        assertThrows(VirtualFileNoStorageSpaceException.class, () -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });
    }

    @Test
    public void testSendFileDeviceToSatellite() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "LaptopDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

        controller.addFileToDevice("DeviceA", "file1", "hello");
        Map<String, FileInfoResponse> expectedA = new HashMap<>();
        expectedA.put("file1", new FileInfoResponse("file1", "hello", "hello".length(), true));

        assertEquals(expectedA, controller.getInfo("DeviceA").getFiles()); // should have 1 file

        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });

        // 1 byte transferred
        controller.simulate();

        Map<String, FileInfoResponse> expectedB = new HashMap<>();
        expectedB.put("file1", new FileInfoResponse("file1", "ello", "hello".length(), false));
        Map<String, FileInfoResponse> expectedC = new HashMap<>();
        expectedC.put("file1", new FileInfoResponse("file1", "h", "hello".length(), false));

        assertEquals(expectedB, controller.getInfo("DeviceA").getFiles());
        assertEquals(expectedC, controller.getInfo("SatelliteA").getFiles());

        // complete file transfer
        controller.simulate("hello".length() - 1);

        Map<String, FileInfoResponse> expectedD = new HashMap<>();
        expectedD.put("file1", new FileInfoResponse("file1", "hello", "hello".length(), true));

        assertEquals(new HashMap<>(), controller.getInfo("DeviceA").getFiles());
        assertEquals(expectedD, controller.getInfo("SatelliteA").getFiles());
    }

    @Test
    public void testSendFileSatelliteToDevice() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "LaptopDevice", Angle.fromDegrees(30));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(30));

        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(30));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceA"));

        controller.addFileToDevice("DeviceA", "file1", "e");
        Map<String, FileInfoResponse> expectedA = new HashMap<>();
        expectedA.put("file1", new FileInfoResponse("file1", "e", "e".length(), true));

        assertEquals(expectedA, controller.getInfo("DeviceA").getFiles()); // should have 1 file
        assertEquals(new HashMap<>(), controller.getInfo("SatelliteA").getFiles());
        assertEquals(new HashMap<>(), controller.getInfo("DeviceB").getFiles());

        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });

        // send file from DeviceA to satellite
        controller.simulate();

        assertEquals(new HashMap<>(), controller.getInfo("DeviceA").getFiles());
        assertEquals(expectedA, controller.getInfo("SatelliteA").getFiles());
        assertEquals(new HashMap<>(), controller.getInfo("DeviceB").getFiles());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.communicableEntitiesInRange("DeviceB"));

        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "SatelliteA", "DeviceB");
        });

        // send file from satellite to DeviceB
        controller.simulate();

        assertEquals(new HashMap<>(), controller.getInfo("DeviceA").getFiles());
        assertEquals(new HashMap<>(), controller.getInfo("SatelliteA").getFiles());
        assertEquals(expectedA, controller.getInfo("DeviceB").getFiles());
    }

    // More send file tests

    public void generateTeleportingSendFileData(BlackoutController controller) {
        controller.createDevice("DeviceA", "LaptopDevice", Angle.fromDegrees(30));
        controller.createSatellite("SatelliteA", "TeleportingSatellite", 100000, Angle.fromDegrees(30));
        controller.addFileToDevice("DeviceA", "file1", "hello");
        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });
        controller.simulate();

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", "hello", "hello".length(), true));

        assertEquals(expected, controller.getInfo("SatelliteA").getFiles());
    }

    public void generateTeleportingSendFileData2(BlackoutController controller) {
        controller.createDevice("DeviceA", "LaptopDevice", Angle.fromDegrees(182));
        controller.createSatellite("SatelliteA", "TeleportingSatellite", 100000, Angle.fromDegrees(182));
        // file transferred after 3 ticks
        String msg = "well hello there, said obi wan kenobi";
        controller.addFileToDevice("DeviceA", "file1", msg);
        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });
        controller.simulate(3);

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", msg, msg.length(), true));

        assertEquals(expected, controller.getInfo("SatelliteA").getFiles());
    }

    @Test
    public void testSendFileDeviceToTeleportingSatellite() {
        BlackoutController controller = new BlackoutController();

        generateTeleportingSendFileData(controller);
    }

    @Test
    public void testSendFileTeleportingSatelliteToSatellite() {
        BlackoutController controller = new BlackoutController();
        generateTeleportingSendFileData(controller);

        controller.createSatellite("SatelliteB", "StandardSatellite", 100000, Angle.fromDegrees(0));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "SatelliteA"),
                controller.communicableEntitiesInRange("SatelliteB"));

        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "SatelliteA", "SatelliteB");
        });
        controller.simulate();

        Map<String, FileInfoResponse> expectedA = new HashMap<>();
        expectedA.put("file1", new FileInfoResponse("file1", "h", "hello".length(), false));
        Map<String, FileInfoResponse> expectedB = new HashMap<>();
        expectedB.put("file1", new FileInfoResponse("file1", "ello", "hello".length(), false));

        assertEquals(expectedB, controller.getInfo("SatelliteA").getFiles());
        assertEquals(expectedA, controller.getInfo("SatelliteB").getFiles());
    }

    @Test
    public void testSendFileTeleportingSatelliteToDevice() {
        BlackoutController controller = new BlackoutController();
        generateTeleportingSendFileData(controller);

        controller.createDevice("DeviceB", "HandheldDevice", Angle.fromDegrees(0));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB"),
                controller.communicableEntitiesInRange("SatelliteA"));

        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "SatelliteA", "DeviceB");
        });
        controller.simulate();

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", "hello", "hello".length(), true));

        assertEquals(expected, controller.getInfo("DeviceB").getFiles()); // should have 1 file
    }

    @Test
    public void testSendFileDeviceToTeleportingHasTeleported() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "LaptopDevice", Angle.fromDegrees(181));
        controller.createSatellite("SatelliteA", "TeleportingSatellite", 100000, Angle.fromDegrees(180.5));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.communicableEntitiesInRange("SatelliteA"));

        String msg = "well hello there, said obi wan kenobi";
        controller.addFileToDevice("DeviceA", "file1", msg);
        // file too long to send in one tick, but since satellite teleports, transfer fails
        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });
        controller.simulate();

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", msg, msg.length(), true));

        assertEquals(new HashMap<>(), controller.getInfo("SatelliteA").getFiles());
        assertEquals(expected, controller.getInfo("DeviceA").getFiles());

    }

    @Test
    public void testSendFileTeleportingToDeviceHasTeleported() {

        BlackoutController controller = new BlackoutController();
        generateTeleportingSendFileData2(controller);

        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(180));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA", "DeviceB"),
                controller.communicableEntitiesInRange("SatelliteA"));

        String msg = "well hello there, said obi wan kenobi";
        // file too long to send in one tick, but since satellite teleports, file instantly transfers
        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "SatelliteA", "DeviceB");
        });
        controller.simulate();

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", msg, msg.length(), true));

        assertEquals(expected, controller.getInfo("DeviceB").getFiles()); // should have 1 file
    }

    @Test
    public void testSendFileViaRelay() {

        BlackoutController controller = new BlackoutController();

        controller.createDevice("DeviceA", "LaptopDevice", Angle.fromDegrees(30));
        controller.createSatellite("SatelliteA", "StandardSatellite", 100000, Angle.fromDegrees(120));
        controller.createSatellite("SatelliteB", "RelaySatellite", 100000, Angle.fromDegrees(60));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceA"), controller.listDeviceIds());
        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB"), controller.listSatelliteIds());

        assertListAreEqualIgnoringOrder(Arrays.asList("SatelliteA", "SatelliteB"),
                controller.communicableEntitiesInRange("DeviceA"));

        controller.addFileToDevice("DeviceA", "file1", "e");
        assertDoesNotThrow(() -> {
            controller.sendFile("file1", "DeviceA", "SatelliteA");
        });
        controller.simulate();

        Map<String, FileInfoResponse> expected = new HashMap<>();
        expected.put("file1", new FileInfoResponse("file1", "e", "e".length(), true));

        assertEquals(expected, controller.getInfo("SatelliteA").getFiles()); // should have 1 file
    }

    // Create Slopes

    @Test
    public void testCreateSlope() {
        BlackoutController controller = new BlackoutController();

        controller.createSlope(30, 50, 720);
        controller.createSlope(50, 70, -510);
    }
}
