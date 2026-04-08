package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.List;

public class DetectObjectsEvent implements Event<List<DetectedObject>> {
    private final StampedDetectedObjects detectedObjects;
    private final int tick;

    public DetectObjectsEvent(StampedDetectedObjects detectedObjects, int tick) {
        this.detectedObjects = detectedObjects;
        this.tick = tick;
    }

    public StampedDetectedObjects getDetectedObjects() {
        return detectedObjects;
    }

    public int getTick() {
        return tick;
    }
}