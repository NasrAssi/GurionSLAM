package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 */
public class Pose {
    private float x;     
    private float y; 
    private float yaw; 
    private int time;  


    public Pose(float x, float y, float yaw, int time) {
        this.x = x;
        this.y = y;
        this.yaw = yaw;
        this.time = time;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getYaw() {
        return yaw;
    }

    public int getTime() {
        return time;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double distanceTo(Pose other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    public float angleDifference(Pose other) {
        return Math.abs(this.yaw - other.yaw);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; 
        if (obj == null || getClass() != obj.getClass()) return false;

        Pose pose = (Pose) obj;
        return Float.compare(pose.x, x) == 0 &&
               Float.compare(pose.y, y) == 0 &&
               Float.compare(pose.yaw, yaw) == 0 &&
               time == pose.time;
    }
}
