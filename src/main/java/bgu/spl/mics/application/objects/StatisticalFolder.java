package bgu.spl.mics.application.objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    private static class StatisticalFolderholder {
        private static StatisticalFolder instance = new StatisticalFolder();
    }
    private int systemRuntime;
    private int numDetectedObjects;  
    private int numTrackedObjects;    
    private int numLandmarks;       
    private Map<String, LandMark> landMarks;

    private StatisticalFolder() {
        this.systemRuntime = 0;
        this.numDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
        landMarks = new ConcurrentHashMap<>();
    }

    public static StatisticalFolder getInstance(){
        return StatisticalFolderholder.instance;
    }


    public synchronized int getSystemRuntime() {
        return systemRuntime;
    }

    public synchronized int getNumDetectedObjects() {
        return numDetectedObjects;
    }

    public synchronized int getNumTrackedObjects() {
        return numTrackedObjects;
    }

    public synchronized int getNumLandmarks() {
        return numLandmarks;
    }

    public synchronized void incrementRuntime() {
        if(FusionSlam.getInstance().getNumberOfSensors() != 0 && !FusionSlam.getInstance().isHasCrash())
            systemRuntime++;
    }

    public synchronized void incrementDetectedObjects(int count) {
        numDetectedObjects += count;
    }

    public synchronized void addTrackedObjects(int size) {
        numTrackedObjects += size;
    }

    public synchronized void incrementLandmarks() {
        numLandmarks += 1;
    }

    public synchronized void resetStatistics() {
        systemRuntime = 0;
        numDetectedObjects = 0;
        numTrackedObjects = 0;
        numLandmarks = 0;
    }

    public synchronized Map<String, LandMark> getLandmarks() {
        return landMarks;
    }

    public synchronized void addLandmarks(LandMark landmark) {
        this.landMarks.put(landmark.getId(), landmark);
    }

}
