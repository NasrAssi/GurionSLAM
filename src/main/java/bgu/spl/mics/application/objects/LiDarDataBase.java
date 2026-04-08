package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    // Fields
    private List<StampedCloudPoints> cloudPoints;
    private int counter;
    private boolean loaded;

    public synchronized int getCounter() {
        return counter;
    }

    // Singleton instance
   private static class LiDarDataBaseHolder {
        private static LiDarDataBase instance = new LiDarDataBase();
    }



    /**
     * Private constructor to enforce singleton pattern.
     */
    private LiDarDataBase() {
        this.cloudPoints = new ArrayList<>();
        counter = 0;
        loaded = false;
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        LiDarDataBaseHolder.instance.loadDataFromFile(filePath);
        return LiDarDataBase.LiDarDataBaseHolder.instance;
        
    }
    


    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }


    public synchronized void addCloudPoint(StampedCloudPoints point) {
        cloudPoints.add(point);
    }

    public synchronized void clearCloudPoints() {
        cloudPoints.clear();
    }

   private synchronized void loadDataFromFile(String dataPath){
        if (loaded) return;
        loaded = true;
 
        try (FileReader reader = new FileReader(dataPath)) {
 
            JsonArray dataArray = JsonParser.parseReader(reader).getAsJsonArray();
            for (int i = 0; i < dataArray.size(); i++) {
 
                JsonObject entry = dataArray.get(i).getAsJsonObject();
 
                int time = entry.get("time").getAsInt();
                String id = entry.get("id").getAsString();
 
                JsonArray cloudPointsArray = entry.getAsJsonArray("cloudPoints");
                List<List<Double>> cloudPointsList = new LinkedList<>();
                for (int j = 0; j < cloudPointsArray.size(); j++) {
 
                    JsonArray pointArray = cloudPointsArray.get(j).getAsJsonArray();
                    List<Double> lst = new LinkedList<>();
 
                    lst.add(pointArray.get(0).getAsDouble());
                    lst.add(pointArray.get(1).getAsDouble());
 
                    cloudPointsList.add(lst);
                }
 
                StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(id, time, cloudPointsList);
                cloudPoints.add(stampedCloudPoints);
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }
    }

    public synchronized void incrementCounter() {
        counter++;
    }

}
