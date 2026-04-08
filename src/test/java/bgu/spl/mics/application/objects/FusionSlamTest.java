package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class FusionSlamTest {


    @BeforeEach
    public void setUp() {
        FusionSlam.getInstance().reset();
    }

    @Test
    public void testAddNewLandMark() {

        FusionSlam fusionSlam = FusionSlam.getInstance();
        StatisticalFolder stats = StatisticalFolder.getInstance();
        stats.resetStatistics();

        LinkedList<CloudPoint> coordinates = new LinkedList<>();
        coordinates.add(new CloudPoint(1.0, 2.0));
        LandMark newMark = new LandMark("L1","Description1", coordinates);

        fusionSlam.addLandmark(newMark);

        assertEquals(1, fusionSlam.getLandmarks().size(), "Landmarks list size should be 1");
        LandMark addedMark = fusionSlam.getLandmarks().get(0);
        assertEquals("L1", addedMark.getId(), "Landmark ID should match");
        assertEquals(1.0, addedMark.getCoordinates().get(0).getX(), "Coordinate X should match");
        assertEquals(2.0, addedMark.getCoordinates().get(0).getY(), "Coordinate Y should match");
    }


    @Test
    public void testUpdateExistingLandMark() {

        FusionSlam fusionSlam = FusionSlam.getInstance();
        StatisticalFolder stats = StatisticalFolder.getInstance();
        stats.resetStatistics();

        LinkedList<CloudPoint> existingCoordinates = new LinkedList<>();
        existingCoordinates.add(new CloudPoint(1.0, 2.0));
        LandMark existingMark = new LandMark("L1","Description", existingCoordinates);
        fusionSlam.addLandmark(existingMark);

        LinkedList<CloudPoint> newCoordinates = new LinkedList<>();
        newCoordinates.add(new CloudPoint(3.0, 4.0));
        LandMark newMark = new LandMark("L1","Description",newCoordinates);

        fusionSlam.addLandmark(newMark);

        assertEquals(1, fusionSlam.getLandmarks().size(), "Landmarks list size should remain 1");
        LandMark updatedMark = fusionSlam.getLandmarks().get(0);
        assertEquals("L1", updatedMark.getId(), "Landmark ID should match");
        assertEquals(2.0, updatedMark.getCoordinates().get(0).getX(), "Updated coordinate X should match the average");
        assertEquals(3.0, updatedMark.getCoordinates().get(0).getY(), "Updated coordinate Y should match the average");
    }

    /**
     * Test case 3: Add a landmark with no coordinates.
     */
    @Test
    public void testAddLandMarkNoCoordinates() {

        FusionSlam fusionSlam = FusionSlam.getInstance();
        StatisticalFolder stats = StatisticalFolder.getInstance();
        stats.resetStatistics();

        LinkedList<CloudPoint> coordinates = new LinkedList<>();
        LandMark newMark = new LandMark("L2","Description", coordinates);


        fusionSlam.addLandmark(newMark);


        assertEquals(1, fusionSlam.getLandmarks().size(), "Landmarks list size should be 1");
        LandMark addedMark = fusionSlam.getLandmarks().get(0);
        assertEquals("L2", addedMark.getId(), "Landmark ID should match");
        assertTrue(addedMark.getCoordinates().isEmpty(), "Coordinates list should be empty");
    }

}