package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {

    private String id;
    private int time;  
    private List<List<Double>> cloudPoints; 

    public StampedCloudPoints(String id, int time, List<List<Double>> cloudPoints) {
        this.id = id;
        this.time = time;
        this.cloudPoints = cloudPoints;
    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public List<CloudPoint> getCloudPoints() {
        List<CloudPoint> list = new ArrayList<>();
        for(List<Double> lst: cloudPoints){
           list.add(new CloudPoint(lst.get(0), lst.get(1)));
        }
        return list;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setCloudPoints(List<List<Double>> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }

    public Double getX(){
        Double x = 0.0;
        for(List<Double> lst : cloudPoints){
            x += lst.get(0);
        }
        return x/cloudPoints.size();
    }

    public Double getY(){
        Double x = 0.0;
        for(List<Double> lst : cloudPoints){
            x += lst.get(1);
        }
        return x/cloudPoints.size();
    }

}
