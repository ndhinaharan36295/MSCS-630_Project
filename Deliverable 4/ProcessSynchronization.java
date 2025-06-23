import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

// --- Process Synchronization (Producer-Consumer with Semaphores) ---
class BoundedBuffer {
    private final Queue<Integer> buffer = new LinkedList<>();    // Buffer to hold produced items
    private final int capacity;                                  // Maximum capacity of the buffer
    private final Semaphore mutex = new Semaphore(1);     // Semaphore (mutex) to ensure mutual exclusion
    private final Semaphore empty;                               // Semaphore to track empty slots
    private final Semaphore full = new Semaphore(0);      // Semaphore to track filled slots

    BoundedBuffer(int capacity) {
        this.capacity = capacity;
        this.empty = new Semaphore(capacity);
    }

    public void produce(int item) throws InterruptedException {
        empty.acquire();
        mutex.acquire();
        buffer.add(item);
        System.out.println("Produced: " + item);
        mutex.release();
        full.release();
    }

    public int consume() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        int item = buffer.poll();
        System.out.println("Consumed: " + item);
        mutex.release();
        empty.release();
        return item;
    }
}
