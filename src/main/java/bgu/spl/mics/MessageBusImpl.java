package bgu.spl.mics;

import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl} class is the implementation of the MessageBus interface.
 * It manages message delivery and event handling for microservices.
 */
public class MessageBusImpl implements MessageBus {
    private static class MessageBusImplHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

    // Singleton instance
    // private static volatile MessageBusImpl instance = null;

    // Thread-safe data structures
    private final ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> micros = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Event<?>, Future<?>> future_events = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends Event<?>>, BlockingQueue<MicroService>> event_map = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends Broadcast>, BlockingQueue<MicroService>> broadcast_map = new ConcurrentHashMap<>();


    /**
     * @PRE None
     * @POST Initializes an empty MessageBusImpl instance.
     */
    private MessageBusImpl() {}

    /**
     * @PRE None
     * @POST Returns the singleton instance of MessageBusImpl.
     */
    public static MessageBusImpl getInstance() {
        return MessageBusImplHolder.instance;
    }

    /**
     * @PRE type != null, m != null
     * @POST Registers the microservice for the given event type.
     */
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
       synchronized(event_map){
        event_map.putIfAbsent(type, new LinkedBlockingQueue<>());
        event_map.get(type).add(m);
       }
    }

    /**
     * @PRE type != null, m != null
     * @POST Registers the microservice for the given broadcast type.
     */
    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        synchronized(broadcast_map){
        broadcast_map.putIfAbsent(type, new LinkedBlockingQueue<>());
        broadcast_map.get(type).add(m);
        }
    }

    /**
     * @PRE e != null, result != null
     * @POST Completes the event with the given result.
     */
    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> future = (Future<T>) future_events.remove(e);
        if (future != null) 
            future.resolve(result);
    }

    /**
     * @PRE b != null
     * @POST Sends the broadcast message to all subscribed microservices.
     */
    @Override
    public void sendBroadcast(Broadcast b) {
        synchronized (broadcast_map) {
            BlockingQueue<MicroService> subscribers = broadcast_map.get(b.getClass());
            if (subscribers != null) {
                for (MicroService m : subscribers) {
                    LinkedBlockingQueue<Message> q = micros.get(m);
                    if (q != null) {
                        q.add(b);
                    }
                }
            }
        }
    }

    /**
     * @PRE e != null
     * @POST Sends the event to a microservice and returns a Future object for the result.
     */
    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        BlockingQueue<MicroService> subscribers = event_map.get(e.getClass());
        if (subscribers == null || subscribers.isEmpty()) {
            return null;
        }
        MicroService m;
        synchronized(subscribers){
            m = subscribers.poll();
            if (m == null) {
                return null;
            }
            subscribers.add(m);
        }

        LinkedBlockingQueue<Message> q = micros.get(m);
        if (q == null) {
            return null;
        }

        Future<T> future = new Future<>();
        future_events.put(e, future);
        q.add(e);
        return future;
    }

    /**
     * @PRE m != null
     * @POST Registers the microservice.
     */
    @Override
    public void register(MicroService m) {
        micros.putIfAbsent(m, new LinkedBlockingQueue<>());
    }

    /**
     * @PRE m != null
     * @POST Unregisters the microservice and resolves pending events.
     */
    @Override
    public void unregister(MicroService m) {
        synchronized (event_map) {
            synchronized (broadcast_map) {
                LinkedBlockingQueue<Message> q = micros.remove(m);
                if (q != null) {
                    while (!q.isEmpty()) {
                        Message msg = q.poll();
                        if (msg instanceof Event) {
                            Future<?> future = future_events.remove(msg);
                            if (future != null) {
                                future.resolve(null);
                            }
                        }
                    }
                }

                for (BlockingQueue<MicroService> broadcast : broadcast_map.values()) {
                    broadcast.remove(m);
                }

                for (BlockingQueue<MicroService> event : event_map.values()) {
                    event.remove(m);
                }
            }
        }
    }

    /**
     * @PRE m is registered
     * @POST Returns the next available message for the microservice, blocking if none are available.
     */
    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        LinkedBlockingQueue<Message> q = micros.get(m);
        if (q == null) {
            throw new IllegalStateException("MicroService is not registered");
        }
        return q.take();
    }
}
