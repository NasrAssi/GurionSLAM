package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    private final int tickTime;
    private final int duration;
    private final StatisticalFolder stats = StatisticalFolder.getInstance();

    /**
     * Constructor for TimeService.
     *
     * @param tickTime  The duration of each tick in milliseconds.
     * @param duration  The total number of ticks before the service terminates.
     */
    public TimeService(int tickTime, int duration) {
        super("TimeService");
        this.tickTime = tickTime;
        this.duration = duration;
    }

    /**
     * Initializes the TimeService.
     * Starts a timer thread that broadcasts ticks, while the main run loop
     * remains available to process incoming messages (e.g., CrashedBroadcast).
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getServiceName().equals(getName())) {
                terminate();
            }
        });

        Thread timerThread = new Thread(() -> {
            for (int tick = 1; tick <= duration; tick++) {
                try {
                    Thread.sleep(tickTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                sendBroadcast(new TickBroadcast(tick));
                stats.incrementRuntime();
            }
            sendBroadcast(new TerminatedBroadcast(getName()));
            System.out.println("TimeService has terminated");
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }
}
