package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private int id;                        
    private int frequency;                          
    private STATUS status;                            
    private String reason;
    private List<StampedDetectedObjects> detectedObjectsList;
    private int detectedStamps;

    /**
     * @PRE id > 0, frequency >= 0, detectedObjectsList != null
     * @POST Initializes camera with status UP, empty reason, and given detection list.
     */
    public Camera(int id, int frequency, List<StampedDetectedObjects> detectedObjectsList) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.reason = "";
        this.detectedObjectsList = detectedObjectsList;
        this.detectedStamps = 0;
    }

    /**
     * @PRE id > 0, frequency >= 0, key != null && !key.isEmpty(), dataPath != null && !dataPath.isEmpty()
     * @POST Initializes camera with status UP, empty reason, and loads data from the specified file.
     */

    //for tests
    public Camera(int id, int frequency, String key, String dataPath) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.reason = "";
        this.detectedStamps = 0;
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, List<StampedDetectedObjects>>>(){}.getType();
        try (FileReader file = new FileReader(dataPath)) {
            Map<String, List<StampedDetectedObjects>> map = gson.fromJson(file, type);
            this.detectedObjectsList = map.get(key);
        } catch (Exception ignored) {}
    }

    /**
     * @PRE None
     * @POST Returns the ID of the camera.
     */
    public int getId() { return id; }

    /**
     * @PRE None
     * @POST Returns the frequency of the camera.
     */
    public int getFrequency() { return frequency; }

    /**
     * @PRE None
     * @POST Returns the status of the camera.
     */
    public STATUS getStatus() { return status; }

    /**
     * @PRE None
     * @POST Returns the list of detected objects.
     */
    public List<StampedDetectedObjects> getDetectedObjectsList() { return detectedObjectsList; }


    /**
     * @PRE frequency >= 0
     * @POST Updates the frequency of the camera.
     */
    public void setFrequency(int frequency) { this.frequency = frequency; }
    
    /**
     * @PRE status != null
     * @POST Updates the status of the camera.
     */
    public void setStatus(STATUS status) { this.status = status; }

    /**
     * @PRE currentTime >= 0
     * @POST Returns detected objects at the given time or null if no objects detected. Updates detected stamps and status.
     */
    public synchronized StampedDetectedObjects detect(int currentTime) {
        List<DetectedObject> objects = new ArrayList<>();

        // Check for ERROR objects at current time
        for (StampedDetectedObjects obj : detectedObjectsList) {
            if (obj.getTime() == currentTime) {
                for (DetectedObject detect : obj.getDetectedObjects()) {
                    if (detect.getId().equals("ERROR")) {
                        reason = detect.getDescription();
                        setStatus(STATUS.ERROR);
                        return null;
                    }
                }
            }
        }

        // Collect detected objects that are ready (time + frequency <= currentTime)
        int originalTime = -1;
        for (StampedDetectedObjects obj : detectedObjectsList) {
            if (obj.getTime() + frequency == currentTime) {
                originalTime = obj.getTime();
                detectedStamps++;
                for (DetectedObject detect : obj.getDetectedObjects()) {
                    if (!detect.getId().equals("ERROR")) {
                        objects.add(detect);
                    }
                }
            }
        }

        if (detectedObjectsList.size() == detectedStamps) {
            setStatus(STATUS.DOWN);
        }
        if (objects.isEmpty()) return null;
        return new StampedDetectedObjects(originalTime, objects);
    }

    /**
     * @PRE object != null
     * @POST Adds the given object to the detected objects list.
     */
    public void addDetectedObject(StampedDetectedObjects object) {
        detectedObjectsList.add(object);
    }

    /**
     * @PRE None
     * @POST Clears the detected objects list.
     */
    public void clearDetectedObjects() {
        detectedObjectsList.clear();
    }

    /**
     * @PRE None
     * @POST Returns the reason for a crash if an error occurred, otherwise returns an empty string.
     */
    public String getCrashReason() {

        return reason;
    }
}
