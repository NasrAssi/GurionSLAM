package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private String id;                         
    private String description;               
    private List<CloudPoint> coordinates;         

    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = new ArrayList<>(coordinates);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoordinates(List<CloudPoint> coordinates) {
        this.coordinates = new ArrayList<>(coordinates); 
    }

    public void addCoordinate(CloudPoint point) {
        coordinates.add(point);
    }

    public void clearCoordinates() {
        coordinates.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        LandMark landmark = (LandMark) obj;
        return id.equals(landmark.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
