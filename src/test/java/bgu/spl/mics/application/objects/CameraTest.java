package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {


    @Test
    public void testValidConstructor() {

        int id = 1;
        int frequency = 5;

        LinkedList<StampedDetectedObjects> detectedObjectsList = new LinkedList<>();
        LinkedList<DetectedObject> objects = new LinkedList<>();
        objects.add(new DetectedObject("obj1", "Test Object 1"));
        detectedObjectsList.add(new StampedDetectedObjects(10, objects));


        Camera camera = new Camera(id, frequency, detectedObjectsList);


        assertEquals(id, camera.getId(), "Camera ID should match");
        assertEquals(frequency, camera.getFrequency(), "Camera frequency should match");
        assertEquals(STATUS.UP, camera.getStatus(), "Camera status should be UP by default");
        assertEquals(detectedObjectsList, camera.getDetectedObjectsList(), "Detected objects list should match");
    }


    @Test
    public void testEmptyDetectedObjectsList() {

        int id = 2;
        int frequency = 10;
        LinkedList<StampedDetectedObjects> detectedObjectsList = new LinkedList<>();


        Camera camera = new Camera(id, frequency, detectedObjectsList);

        // Assertions
        assertEquals(id, camera.getId(), "Camera ID should match");
        assertEquals(frequency, camera.getFrequency(), "Camera frequency should match");
        assertEquals(STATUS.UP, camera.getStatus(), "Camera status should be UP by default");
        assertTrue(camera.getDetectedObjectsList().isEmpty(), "Detected objects list should be empty");
    }



    @Test
    public void testMultipleEntriesInDetectedObjectsList() {

        int id = 4;
        int frequency = 0;

        LinkedList<StampedDetectedObjects> detectedObjectsList = new LinkedList<>();
        LinkedList<DetectedObject> objects1 = new LinkedList<>();
        objects1.add(new DetectedObject("obj1", "Description 1"));

        LinkedList<DetectedObject> objects2 = new LinkedList<>();
        objects2.add(new DetectedObject("obj2", "Description 2"));

        StampedDetectedObjects object1 = new StampedDetectedObjects(5, objects1);
        detectedObjectsList.add(object1);

        StampedDetectedObjects object2 = new StampedDetectedObjects(10, objects2);
        detectedObjectsList.add(object2);


        Camera camera = new Camera(id, frequency, detectedObjectsList);


        assertEquals(id, camera.getId(), "Camera ID should match");
        assertEquals(frequency, camera.getFrequency(), "Camera frequency should match");
        assertEquals(STATUS.UP, camera.getStatus(), "Camera status should be UP by default");
        assertEquals(detectedObjectsList, camera.getDetectedObjectsList(), "Detected objects list should match");
        assertEquals(2, camera.getDetectedObjectsList().size(), "Detected objects list size should be 2");
        assertEquals(object1.getDetectedObjects(), camera.detect(5).getDetectedObjects());
        assertEquals(object2.getDetectedObjects(), camera.detect(10).getDetectedObjects());
    }



    @Test
    public void testGetObjectValidTime() {

        String dataPath = new File("src/test/resources/testCamera.json").getAbsolutePath();
        int id = 1;
        int frequency = 0;
        String key = "camera1";


        Camera camera = new Camera(id, frequency, key, dataPath);

        StampedDetectedObjects result = camera.detect(2);

        assertNotNull(result, "Result should not be null for time 2");
        assertEquals(2, result.getTime(), "Time should match the requested time");
        assertEquals(1, result.getDetectedObjects().size(), "Detected objects size should be 1");
        assertEquals("Wall_1", result.getDetectedObjects().get(0).getId(), "First object's ID should match");
        assertEquals("Wall", result.getDetectedObjects().get(0).getDescription(), "First object's description should match");
    }


    @Test
    public void testGetObjectAnotherValidTime() {


        String dataPath = new File("src/test/resources/testCamera.json").getAbsolutePath();
        int id = 1;
        int frequency = 0;
        String key = "camera1";


        Camera camera = new Camera(id, frequency, key, dataPath);


        StampedDetectedObjects result = camera.detect(4);


        assertNotNull(result, "Result should not be null for time 4");
        assertEquals(4, result.getTime(), "Time should match the requested time");
        assertEquals(2, result.getDetectedObjects().size(), "Detected objects size should be 2");


        assertEquals("Wall_3", result.getDetectedObjects().get(0).getId(), "First object's ID should match");
        assertEquals("Wall", result.getDetectedObjects().get(0).getDescription(), "First object's description should match");


        assertEquals("Chair_Base_1", result.getDetectedObjects().get(1).getId(), "Second object's ID should match");
        assertEquals("Chair Base", result.getDetectedObjects().get(1).getDescription(), "Second object's description should match");
    }


    @Test
    public void testGetObjectInvalidTime() {

        String dataPath = new File("src/test/resources/testCamera.json").getAbsolutePath();
        int id = 1;
        int frequency = 5;
        String key = "camera1";

        Camera camera = new Camera(id, frequency, key, dataPath);
        StampedDetectedObjects result = camera.detect(99);
        assertNull(result, "Result should be null for an invalid time");
    }

}