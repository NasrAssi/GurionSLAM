package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private String serviceName;
    private String reason;

    public CrashedBroadcast(String serviceName, String reason) {
        this.serviceName = serviceName;
        this.reason = reason;
    }

    public String getServiceName() {
        return serviceName;
    }
    
    public String getReason() {
        return reason;
    }
}
