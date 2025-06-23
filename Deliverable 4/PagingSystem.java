import java.util.*;

// --- Memory Management (Paging Simulation) ---
class Page {
    int pageNumber;     // Unique identifier for the page
    int lastUsedTime;   // Time when the page was last accessed (used for LRU replacement algorithm)

    Page(int pageNumber, int time) {
        this.pageNumber = pageNumber;
        this.lastUsedTime = time;
    }
}

public class PagingSystem {
    int capacity;               // Maximum number of pages that can be held in memory
    Map<Integer, Page> memory;  // Map to store pages currently held in memory
    Queue<Integer> fifoQueue;   // Queue to manage FIFO page replacement
    int time;                   // Current time (used for LRU and FIFO)
    int pageFaults;             // Counter for page faults

    PagingSystem(int capacity) {
        this.capacity = capacity;
        this.memory = new HashMap<>();
        this.fifoQueue = new LinkedList<>();
        this.time = 0;
        this.pageFaults = 0;
    }

    // Simulates accessing a page using FIFO page replacement algorithm
    public void accessPageFIFO(int pageNumber) {
        time++;
        // Check if the page is already in memory
        // if doesn't exist, it's a page fault
        if (!memory.containsKey(pageNumber)) {
            pageFaults++;
            if (memory.size() >= capacity) {
                // Remove the oldest page from memory (based on FIFO algorithm)
                int oldest = fifoQueue.poll();
                memory.remove(oldest);
                System.out.println("Memory overflow. Page " + oldest + " will be replaced. ");
            }
            // Add the new page to memory and the FIFO queue
            memory.put(pageNumber, new Page(pageNumber, time));
            fifoQueue.add(pageNumber);
            System.out.println("FIFO Page Fault. Page " + pageNumber + " loaded to memory.");
        } else {
            // If the page is already in memory, it's a Page hit and the page is accessed
            System.out.println("FIFO Hit: Page " + pageNumber + " accessed.");
        }
    }

    // Simulates accessing a page using LRU page replacement algorithm
    public void accessPageLRU(int pageNumber) {
        time++;
        // Check if the page is already in memory
        // if doesn't exist, it's a page fault
        if (!memory.containsKey(pageNumber)) {
            pageFaults++;
            if (memory.size() >= capacity) {
                // Find least recently used page
                int lru = memory
                        .entrySet()
                        .stream()
                        .min(Comparator.comparingInt(e -> e.getValue().lastUsedTime))
                        .get()
                        .getKey();

                // Remove the least recently used page from memory to manage memory overflow
                memory.remove(lru);
                System.out.println("Memory overflow. Page " + lru + " will be replaced. ");
            }
            // Add the new page to memory
            memory.put(pageNumber, new Page(pageNumber, time));
            System.out.println("LRU Page Fault - Page " + pageNumber + " loaded to memory.");
        } else {
            // If the page is already in memory, it's a Page hit and the page is accessed
            // Update the last used time for the page -- this is crucial for LRU replacement algorithm
            memory.get(pageNumber).lastUsedTime = time;
            System.out.println("LRU Hit - Page " + pageNumber + " accessed.");
        }
    }

    // Returns the total number of page faults
    public int getPageFaults() {
        return pageFaults;
    }
}
