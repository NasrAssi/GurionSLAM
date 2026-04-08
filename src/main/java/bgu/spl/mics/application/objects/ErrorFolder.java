package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ErrorFolder {
    private static class ErrorFolderHolder {
        private static ErrorFolder instance = new ErrorFolder();
    }

    private String error;
    private String faultySensor;
    private Map<String, StampedDetectedObjects> lastCamerasFrame;
    private Map<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame;
    private List<Pose> poses;
    private StatisticalFolder statistics;

    public static ErrorFolder getInstance(){
        return ErrorFolder.ErrorFolderHolder.instance;
    }
    private ErrorFolder(){
        this.error = "";
        this.faultySensor = "";
        this.lastCamerasFrame = new ConcurrentHashMap<>();
        this.lastLiDarWorkerTrackersFrame = new ConcurrentHashMap<>();
        this.poses = new ArrayList<>();
        this.statistics = StatisticalFolder.getInstance();
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setFaultySensor(String faultySensor) {
        this.faultySensor = faultySensor;
    }

    public synchronized void addCameraFrame(String name, StampedDetectedObjects objects){
        lastCamerasFrame.put(name, objects);
    }

    public synchronized void addLiDarFrame(String name, List<TrackedObject> objects){
        lastLiDarWorkerTrackersFrame.put(name, objects);
    }

    public synchronized void addPose(Pose p){
        poses.add(p);
    }
}
