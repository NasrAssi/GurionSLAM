package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.ErrorFolder;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GurionRockRunner <configuration file path>");
            return;
        }

        try {
            String path = Paths.get(args[0]).getParent().toFile().getAbsolutePath();
           
            JsonObject config;
            try (FileReader configReader = new FileReader(args[0])) {
                config = JsonParser.parseReader(configReader).getAsJsonObject();
            }

      
            int tickTime = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();

           
            GPSIMU gpsimu = new GPSIMU();
            JsonArray poses;
            try (java.io.BufferedReader poseReader = Files.newBufferedReader(Paths.get(path, config.get("poseJsonFile").getAsString()))) {
                poses = JsonParser.parseReader(poseReader).getAsJsonArray();
            }
            for (JsonElement poseElem : poses) {
                JsonObject poseObj = poseElem.getAsJsonObject();
                gpsimu.addPose(new Pose(
                        poseObj.get("x").getAsFloat(),
                        poseObj.get("y").getAsFloat(),
                        poseObj.get("yaw").getAsFloat(),
                        poseObj.get("time").getAsInt()
                ));
            }

       
            FusionSlam fusionSlam = FusionSlam.getInstance();

          
            JsonObject LiDarWorkersConfig = config.get("LiDarWorkers").getAsJsonObject();
            JsonArray lidarConfig = LiDarWorkersConfig.get("LidarConfigurations").getAsJsonArray();
            LiDarDataBase instance = LiDarDataBase.getInstance(
                Paths.get(path, LiDarWorkersConfig.get("lidars_data_path").getAsString()).toString()
            );

            List<LiDarWorkerTracker> LidarList = new ArrayList<>();
            for (JsonElement lidarConfigX : lidarConfig) {
                JsonObject lidarObj = lidarConfigX.getAsJsonObject();

              
                if (!lidarObj.has("id") || !lidarObj.has("frequency")) {
                    throw new IllegalArgumentException("Invalid LiDAR configuration: " + lidarObj.toString());
                }

               
                LiDarWorkerTracker lidarTracker = new LiDarWorkerTracker(
                    lidarObj.get("id").getAsInt(),
                    lidarObj.get("frequency").getAsInt(),
                    instance
                );

                LidarList.add(lidarTracker);
            }

           
            JsonArray cameras = config.get("Cameras").getAsJsonObject().get("CamerasConfigurations").getAsJsonArray();
            Gson g = new GsonBuilder().setPrettyPrinting().create();
            Type t = new TypeToken<Map<String, StampedDetectedObjects[]>>(){}.getType();
            Map<String, StampedDetectedObjects[]> camerasData;
            try (Reader r = Files.newBufferedReader(
                Paths.get(path, config.get("Cameras").getAsJsonObject().get("camera_datas_path").getAsString())
            )) {
                camerasData = g.fromJson(r, t);
            }

            List<Camera> cameraList = new ArrayList<>();
            for (JsonElement camElem : cameras) {
                JsonObject camObj = camElem.getAsJsonObject();
                String camera_key = camObj.get("camera_key").getAsString();
                StampedDetectedObjects[] objs = camerasData.get(camera_key);
                if(objs == null){
                    continue;
                }

                cameraList.add(new Camera(
                        camObj.get("id").getAsInt(),
                        camObj.get("frequency").getAsInt(),
                        Arrays.asList(objs)
                ));
            }

          
            List<Thread> serviceThreads = new ArrayList<>();
            TimeService timeService = new TimeService(tickTime*1000, duration); 
            serviceThreads.add(new Thread(new PoseService(gpsimu)));
            serviceThreads.add(new Thread(new FusionSlamService()));

           
            for (LiDarWorkerTracker worker : LidarList) {
                serviceThreads.add(new Thread(new LiDarService(worker)));
            }

           
            for (Camera camera : cameraList) {
                serviceThreads.add(new Thread(new CameraService(camera)));
            }

          
            FusionSlam.getInstance().setNumberOfSensors(serviceThreads.size() - 1);

          
            serviceThreads.add(new Thread(timeService));

            for (Thread thread : serviceThreads) {
                thread.start();
            }

            for (Thread thread : serviceThreads) {
                thread.join();
            }
            StatisticalFolder stats = StatisticalFolder.getInstance();
            

            System.out.println("Simulation completed.");

            for(LandMark m : FusionSlam.getInstance().getLandmarks()){
                StatisticalFolder.getInstance().addLandmarks(m);
            }

            if(FusionSlam.getInstance().isHasCrash()) {
                try (FileWriter writer = new FileWriter(Paths.get(path, "error_output.json").toString())) {
                    g.toJson(ErrorFolder.getInstance(), writer);
                }
            } else {
                try (FileWriter writer = new FileWriter(Paths.get(path, "output.json").toString())) {
                    g.toJson(stats, writer);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred during simulation setup or execution.");
        }
    }
}
