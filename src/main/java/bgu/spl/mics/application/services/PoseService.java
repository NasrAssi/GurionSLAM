package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ErrorFolder;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private GPSIMU gpsimu;      // GPS/IMU sensor object

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {

       
        subscribeBroadcast(TickBroadcast.class, tick -> {
            Pose currentPose = gpsimu.getCurrentPose(tick.getTickCount());

            if (currentPose != null) {
                PoseEvent poseEvent = new PoseEvent(currentPose);
                sendEvent(poseEvent);
                ErrorFolder.getInstance().addPose(currentPose);
            }

            if (gpsimu.getStatus() == STATUS.DOWN) {
                System.out.println("[PoseService] terminating at time " + tick.getTickCount());
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });
        

        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            terminate();
        });

        // Subscribe to TerminatedBroadcast for graceful shutdown
        subscribeBroadcast(TerminatedBroadcast.class, terminate -> {
            if(terminate.getServiceName().equals("TimeService")){
                terminate();
                gpsimu.setStatus(STATUS.DOWN);
            }
        });
    }
}
