package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.ErrorFolder;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private Camera camera;        // Camera object
    private int currentTick = 0;  // Current tick time

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera" + camera.getId());
        this.camera = camera;  
    }
    
 
    @Override
    protected void initialize() {

      
        subscribeBroadcast(TickBroadcast.class, tick -> {
            currentTick = tick.getTickCount(); 
           
            StampedDetectedObjects stampedObjects = camera.detect(currentTick);
            if (stampedObjects != null) {
                
                DetectObjectsEvent event = new DetectObjectsEvent(stampedObjects, currentTick);
                sendEvent(event);
                ErrorFolder.getInstance().addCameraFrame(getName(), event.getDetectedObjects());
            
                StatisticalFolder.getInstance().incrementDetectedObjects(stampedObjects.getDetectedObjects().size());
            }
            if (camera.getStatus() == STATUS.DOWN) {
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
            if (camera.getStatus() == STATUS.ERROR) {
                sendBroadcast(new CrashedBroadcast(getName(), camera.getCrashReason()));
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            terminate();
        });
    
      
        subscribeBroadcast(TerminatedBroadcast.class, terminate -> {
            if(terminate.getServiceName().equals("TimeService")){
                camera.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });
    }
}
