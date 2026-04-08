package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;

import java.util.List;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {

    private FusionSlam fusionSlam; // Global mapping manager

    /**
     * Constructor for FusionSlamService.
     */
    public FusionSlamService() {
        super("FusionSlamService");
        this.fusionSlam = FusionSlam.getInstance(); // Singleton instance
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle events and broadcasts.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            System.out.println(crashed.getServiceName() + " crashed due " + crashed.getReason());
            fusionSlam.setHasCrash(true);
            ErrorFolder.getInstance().setError(crashed.getReason());
            ErrorFolder.getInstance().setFaultySensor(crashed.getServiceName());
            terminate();
        });
       
        subscribeEvent(TrackedObjectsEvent.class, event -> {
            List<TrackedObject> trackedObjects = event.getTrackedObjects();
            fusionSlam.addTrackedObjects(event.getTrackedObjects());
            fusionSlam.processTrackedObjects();
        });

        
        subscribeEvent(PoseEvent.class, event -> {
            Pose pose = event.getPose();
            fusionSlam.addPose(pose);
            fusionSlam.processTrackedObjects();
        });
      
        subscribeBroadcast(TerminatedBroadcast.class, terminate -> {
            if (!terminate.getServiceName().equals("TimeService")) {
                fusionSlam.decrementNumberOfSensors();
                if (fusionSlam.getNumberOfSensors() == 0) {
                    System.out.println("[FusionSlamService] terminated");
                    terminate();
                }
            }
        });
    }
}
