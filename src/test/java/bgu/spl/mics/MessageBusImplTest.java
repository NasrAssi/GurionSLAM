package bgu.spl.mics;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
 
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
 
class MessageBusTest {
 
    private MessageBus messageBus;
    private MicroService testService1;
    private MicroService testService2;
    private Event<String> testEvent;
    private Broadcast testBroadcast;
 
    @BeforeEach
    void setup() {
        messageBus = MessageBusImpl.getInstance();
        testService1 = new MicroService("TestService1") {
            @Override
            protected void initialize() {
            }
        };
        testService2 = new MicroService("TestService2") {
            @Override
            protected void initialize() {
            }
        };
        testEvent = new Event<String>() {};
        testBroadcast = new Broadcast() {};
    }
 
    @Test
    void testRegister() {
        assertDoesNotThrow(() -> messageBus.register(testService1));
    }
 
    @Test
    void testUnregister() {
        messageBus.register(testService1);
        messageBus.unregister(testService1);
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testService1));
    }
 
    @Test
    void testSubscribeEvent() throws InterruptedException {
        messageBus.register(testService1);
        messageBus.subscribeEvent((Class<? extends Event<String>>) testEvent.getClass(), testService1);
 
        Future<String> future = messageBus.sendEvent(testEvent);
        assertNotNull(future);
 
        Message receivedMessage = messageBus.awaitMessage(testService1);
        assertEquals(testEvent, receivedMessage);
    }
 
    @Test
    void testSubscribeBroadcast() throws InterruptedException {
        messageBus.register(testService1);
        messageBus.subscribeBroadcast(testBroadcast.getClass(), testService1);
 
        messageBus.sendBroadcast(testBroadcast);
 
        Message receivedMessage = messageBus.awaitMessage(testService1);
        assertEquals(testBroadcast, receivedMessage);
    }
 
    @Test
    void testSendEventWithNoSubscribers() {
        Future<String> future = messageBus.sendEvent(testEvent);
        assertNull(future, "sendEvent should return null if no MicroService is subscribed.");
    }
 
    @Test
    void testSendBroadcastToMultipleServices() throws InterruptedException {
        messageBus.register(testService1);
        messageBus.register(testService2);
 
        messageBus.subscribeBroadcast(testBroadcast.getClass(), testService1);
        messageBus.subscribeBroadcast(testBroadcast.getClass(), testService2);
 
        messageBus.sendBroadcast(testBroadcast);
 
        Message receivedMessage1 = messageBus.awaitMessage(testService1);
        Message receivedMessage2 = messageBus.awaitMessage(testService2);
 
        assertEquals(testBroadcast, receivedMessage1);
        assertEquals(testBroadcast, receivedMessage2);
    }
 
    @Test
    void testCompleteEvent() {
        messageBus.register(testService1);
        messageBus.subscribeEvent((Class<? extends Event<String>>) testEvent.getClass(), testService1);
 
        Future<String> future = messageBus.sendEvent(testEvent);
        assertNotNull(future);
 
        String result = "Event completed";
        messageBus.complete(testEvent, result);
 
        assertTrue(future.isDone());
        assertEquals(result, future.get());
    }
 
    @Test
    void testAwaitMessageBlocking() throws InterruptedException {
        // Register and subscribe the MicroService
        messageBus.register(testService1);
        messageBus.subscribeBroadcast(testBroadcast.getClass(), testService1);
 
        // Create a CountDownLatch to ensure synchronization
        CountDownLatch latch = new CountDownLatch(1);
 
        // Start a thread to await a message
        Thread testThread = new Thread(() -> {
            try {
                // Signal the latch when waiting starts
                latch.countDown();
                Message message = messageBus.awaitMessage(testService1);
                assertNotNull(message, "Message should not be null after unblocking.");
                assertEquals(testBroadcast, message, "Received message should match the broadcast sent.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted unexpectedly.");
            }
        });
        testThread.start();
 
        // Wait for the thread to start waiting
        latch.await(500, TimeUnit.MILLISECONDS);
 
        // Ensure the thread is waiting for a message
        assertTrue(testThread.isAlive(), "Thread should still be waiting for a message.");
 
        // Send a broadcast to unblock the thread
        messageBus.sendBroadcast(testBroadcast);
 
        // Give time for the thread to process the message
        testThread.join(500);
        assertFalse(testThread.isAlive(), "Thread should no longer be waiting after receiving a message.");
    }
 
    @Test
    void testAwaitMessageWaitsWhenNoMessage() throws InterruptedException {
        // Register the MicroService
        messageBus.register(testService1);
        messageBus.subscribeBroadcast(testBroadcast.getClass(), testService1);
 
        // Create a thread to simulate awaiting a message
        Thread testThread = new Thread(() -> {
            try {
                // This should block until a message is added
                Message message = messageBus.awaitMessage(testService1);
                assertNotNull(message, "Message should not be null after being received.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        testThread.start();
 
        // Ensure the thread is waiting
        TimeUnit.MILLISECONDS.sleep(100);
        assertTrue(testThread.isAlive(), "Thread should be waiting because there is no message.");
 
        // Send a broadcast to unblock the thread
        messageBus.sendBroadcast(testBroadcast);
 
        // Wait longer to ensure processing completes
        TimeUnit.MILLISECONDS.sleep(500);
        assertFalse(testThread.isAlive(), "Thread should no longer be waiting after receiving a message.");
    }
 
    @Test
    void testAwaitMessageDoesNotWaitWhenMessageExists() throws InterruptedException {
        messageBus.register(testService1);
 
        // Add a message to the queue before the thread starts waiting
        messageBus.subscribeBroadcast(testBroadcast.getClass(), testService1);
        messageBus.sendBroadcast(testBroadcast);
 
        // Start a thread to retrieve the message
        Thread testThread = new Thread(() -> {
            try {
                Message message = messageBus.awaitMessage(testService1);
                assertNotNull(message, "Message should not be null because it was already in the queue.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        testThread.start();
 
        // Give the thread time to process the message
        TimeUnit.MILLISECONDS.sleep(100);
        assertFalse(testThread.isAlive(), "Thread should not be waiting because a message was already available.");
    }
 
}