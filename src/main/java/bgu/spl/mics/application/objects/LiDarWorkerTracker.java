package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */

public class LiDarWorkerTracker {
    private int id;
    private int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;  
    private LiDarDataBase instance;

    public LiDarWorkerTracker(int id, int frequency, LiDarDataBase instance) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;  
        this.lastTrackedObjects = new ArrayList<>();
        this.instance = instance;
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<TrackedObject> getTrackedAtTick(int currentTick){
        List<TrackedObject> lst = new ArrayList<>();
        for(TrackedObject tracked : lastTrackedObjects){
            if(tracked.getTime() + frequency <= currentTick){
                lst.add(tracked);
            }
        }
        lastTrackedObjects.removeAll(lst);

       

        if (instance.getCounter() == instance.getCloudPoints().size() && lastTrackedObjects.isEmpty()) {
            setStatus(STATUS.DOWN);
        }
        return lst;
    }
    public void addStampedObjects(StampedDetectedObjects obj, int tickCount) {
        for (DetectedObject detect : obj.getDetectedObjects()) {
            for (StampedCloudPoints point : instance.getCloudPoints()) {
                if (point.getTime() == obj.getTime() && detect.getId().equals(point.getId())) {
                    lastTrackedObjects.add(new TrackedObject(detect.getId(), point.getTime(), detect.getDescription(), point.getCloudPoints()));
                    // Increment the processing counter in the instance
                    instance.incrementCounter();
                }
            }
        }
    }

    public void addAllTrackedObjects(List<TrackedObject> objects) {
        lastTrackedObjects.addAll(objects);
    }

    public void clearTrackedObjects() {
        lastTrackedObjects.clear();
    }

}
