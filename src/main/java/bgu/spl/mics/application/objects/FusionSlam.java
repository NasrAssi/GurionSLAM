package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }

    // Global data storage
    private List<LandMark> landmarks; // List of landmarks
    private List<Pose> poses;              // List of robot poses
    private final AtomicInteger numberOfSensors = new AtomicInteger(0);
    private List<TrackedObject> trackedObjects;
    private volatile boolean hasCrash;

    /**
     * Private constructor for singleton pattern.
     */

      /**
     * @PRE None
     * @POST Initializes an empty FusionSlam instance with no landmarks, poses, or tracked objects.
     */
    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.trackedObjects = new ArrayList<>();
        this.hasCrash = false;
    }

    /**
     * @PRE None
     * @POST Resets all data in FusionSlam.
     */
    public void reset() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.trackedObjects = new ArrayList<>();
        this.hasCrash = false;
    }

    /**
     * @PRE None
     * @POST Returns the crash status.
     */
    public boolean isHasCrash() {
        return hasCrash;
    }

    /**
     * @PRE None
     * @POST Updates the crash status.
     */
    public void setHasCrash(boolean hasCrash) {
        this.hasCrash = hasCrash;
    }

    /**
     * Provides access to the singleton instance of FusionSlam.
     *
     * @return The single instance of FusionSlam.
     */

    /**
     * @PRE None
     * @POST Returns the singleton instance of FusionSlam.
     */
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    /**
     * @PRE None
     * @POST Processes tracked objects and updates landmarks.
     */
    public synchronized void processTrackedObjects(){
        List<TrackedObject> tempList = new ArrayList<>(trackedObjects);
        trackedObjects.clear();
        for(TrackedObject object : tempList){
            Pose p = getPoseAtTime(object.getTime());
            if(p == null) {
                trackedObjects.add(object);
                continue;
            }
            LandMark newlandMark = new LandMark(object.getId(),object.getDescription(), transform(object.getCoordinates(), p));
            if (addLandmark(newlandMark)) {
                StatisticalFolder.getInstance().incrementLandmarks();
            }
        }
    }

    private List<CloudPoint> transform(List<CloudPoint> coordinates, Pose p) {
        List<CloudPoint> newCoords = new ArrayList<>();
        double cosYaw = Math.cos(Math.toRadians(p.getYaw()));
        double sinYaw = Math.sin(Math.toRadians(p.getYaw()));

        for (CloudPoint point : coordinates) {
            double transformedX = cosYaw * point.getX() - sinYaw * point.getY() + p.getX();
            double transformedY = sinYaw * point.getX() + cosYaw * point.getY() + p.getY();
            newCoords.add(new CloudPoint(transformedX, transformedY));
        }
        return newCoords;
    }

    private Pose getPoseAtTime(int time) {
        for(Pose p : poses){
            if(p.getTime() == time){
                return p;
            }
        }
        return null;
    }

    /**
     * Adds a landmark to the global map.
     *
     * @param object The tracked object to be added as a landmark.
     */

    /**
     * @PRE object != null
     * @POST Adds the landmark to the global map or updates an existing one.
     */
    public synchronized Boolean addLandmark(LandMark object) {
        LandMark oldM = alreadyExist(object.getId());
        if (oldM != null) {
            LinkedList<CloudPoint> resultCoordinates = new LinkedList<>();

            int minSize = Math.min(object.getCoordinates().size(), oldM.getCoordinates().size());
            for (int i = 0; i < minSize; i++) {
                double x = (object.getCoordinates().get(i).getX() + oldM.getCoordinates().get(i).getX()) / 2;
                double y = (object.getCoordinates().get(i).getY() + oldM.getCoordinates().get(i).getY()) / 2;
                resultCoordinates.add(new CloudPoint(x, y));
            }
            oldM.setCoordinates(resultCoordinates);
            return false;
        } else {
            landmarks.add(object);
            return true;
        }
    }


    /**
     * Returns the list of landmarks.
     *
     * @return List of tracked objects representing landmarks.
     */

    /**
     * @PRE None
     * @POST Returns the list of landmarks.
     */
    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    /**
     * Adds a pose to the list of robot poses.
     *
     * @param pose The robot's pose to be added.
     */
    public synchronized void addPose(Pose pose) {
        poses.add(pose);
    }

    /**
     * Returns the list of poses.
     *
     * @return List of poses.
     */
    public synchronized List<Pose> getPoses() {
        return poses;
    }

    /**
     * Clears all stored landmarks and poses.
     */
    public void clear() {
        landmarks.clear();
        poses.clear();
    }

    public synchronized LandMark alreadyExist(String id) {
        for (LandMark landMark : landmarks) {
            if (landMark.getId().equals(id)) {
                return landMark;
            }
        }
        return null;
    }

    public void decrementNumberOfSensors() {
        numberOfSensors.decrementAndGet();
    }

    public int getNumberOfSensors() {
        return numberOfSensors.get();
    }

    public void setNumberOfSensors(int count) {
        this.numberOfSensors.set(count);
    }

    public synchronized void addTrackedObjects(List<TrackedObject> trackedObjects) {
        this.trackedObjects.addAll(trackedObjects);
    }

}
