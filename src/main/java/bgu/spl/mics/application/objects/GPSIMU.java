package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GPSIMU represents a GPS/IMU sensor that tracks the robot's pose.
 */
public class GPSIMU {
    private STATUS status;
    private List<Pose> poses; // List of recorded poses

    /**
     * Constructor initializes the GPSIMU with an empty pose list.
     */
    public GPSIMU() {
        this.poses = new ArrayList<>();
        status = STATUS.UP;
    }

    /**
     * Adds a new pose to the list of poses.
     *
     * @param pose The pose to be added.
     */
    public  void addPose(Pose pose) {
        poses.add(pose);
    }

    /**
     * Sets the status of the GPSIMU.
     *
     * @param status The new status.
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

    /**
     * Gets the status of the GPSIMU.
     *
     * @return The current status.
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Returns the current pose of the robot based on the given time.
     *
     * @param time The time for which the pose is requested.
     * @return The current pose or null if no matching pose is found.
     */
    public  synchronized Pose getCurrentPose(int time) {
        // Skip processing if status is already DOWN
        if (status == STATUS.DOWN) {
            return null;
        }

        Iterator<Pose> iterator = poses.iterator();
        while (iterator.hasNext()) {
            Pose p = iterator.next();
            if (p.getTime() == time) {
                iterator.remove();
                if (poses.isEmpty()) {
                    setStatus(STATUS.DOWN); // Update status if no more poses
                }
                return p;
            }
        }

        return null;
    }

}
