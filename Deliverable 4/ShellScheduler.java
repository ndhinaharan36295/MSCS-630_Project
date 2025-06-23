import java.util.*;

class ScheduledProcess implements Comparable<ScheduledProcess> {
    int pid;                // Process ID
    int priority;           // Priority of the process (lower number means higher priority)
    String command;         // Command to execute
    int burstTime;          // Total time required for the process
    int remainingTime;      // Remaining time for the process
    int arrivalTime;        // Time at which the process arrives in the queue
    int startTime;          // Time at which the process starts execution
    int completionTime;     // Time at which the process completes execution

    // Constructor to initialize a scheduled process
    public ScheduledProcess(int pid, int priority, String command, int burstTime, int arrivalTime) {
        this.pid = pid;
        this.priority = priority;
        this.command = command;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;  // Initially, remaining time is equal to burst time
        this.priority = priority;
        this.arrivalTime = arrivalTime;
        this.startTime = -1;
    }

    // Method to compare processes based on priority (used for priority-based scheduling)
    @Override
    public int compareTo(ScheduledProcess o) {
        return Integer.compare(this.priority, o.priority);
    }
}

// This code implements a simple shell scheduler that can perform Round-Robin and Priority-Based scheduling.
public class ShellScheduler {
    public static void addProcessesToQueue(List<ScheduledProcess> processes, int time, Queue<ScheduledProcess> queue) {
        for (Iterator<ScheduledProcess> it = processes.iterator(); it.hasNext();) {
            ScheduledProcess p = it.next();

            if (p.arrivalTime <= time) {
                queue.add(p);
                it.remove();
            }
        }
    }

    // Implements Round-Robin scheduling algorithm
    public static void roundRobinScheduling(List<ScheduledProcess> processes, int quantum) throws InterruptedException {
        // Queue to manage processes in Round-Robin fashion
        Queue<ScheduledProcess> queue = new LinkedList<>();
        // Tracks the current time in the scheduler
        int time = 0;

        System.out.println("\n--- Round-Robin Scheduling ---\n");

        // Continue until all processes are completed
        while (!processes.isEmpty() || !queue.isEmpty()) {
            // Add processes to the queue based on their arrival time
            addProcessesToQueue(processes, time, queue);

            // Execute the process at the front of the queue
            if (!queue.isEmpty()) {
                ScheduledProcess current = queue.poll();

                // Determine execution time (whichever is lesser between quantum slice or remaining time)
                int executeTime = Math.min(quantum, current.remainingTime);

                System.out.println("Time " + time + ": Executing " + current.command + " (PID: " + current.pid + ") for " + executeTime + "s");

                // If this is the first time the process is being executed, set its start time
                if (current.startTime == -1) {
                    current.startTime = time;
                }

                // Simulate process execution with a sleep
                Thread.sleep(executeTime * 500);

                // Update current time
                time += executeTime;
                // Update remaining time for the process (will be non-zero if quantum was less than remaining time)
                current.remainingTime -= executeTime;

                // after execution is completed

                // Add processes to the queue -- if any processes have arrived duringe execution time
                addProcessesToQueue(processes, time, queue);
                // If remaining time is non-zero, the process is not completed. add the process back to the queue
                if (current.remainingTime > 0) {
                    queue.add(current);
                } else {
                    // If the process is completed, set its completion time
                    current.completionTime = time;

                    System.out.println("Time " + time + ": " + current.command + " completed.\n");
                }
            } else {
                // If no process is ready at the current timestamp, the scheduler is idle
                System.out.println("Time " + time + ": Idle");
                Thread.sleep(500);
                time++;
            }
        }
    }

    // Implements Priority-Based scheduling algorithm
    public static void priorityBasedScheduling(List<ScheduledProcess> processes) throws InterruptedException {
        List<ScheduledProcess> allProcesses = new ArrayList<>(processes);
        // Priority queue to manage processes based on their priority
        PriorityQueue<ScheduledProcess> pq = new PriorityQueue<>();
        // Tracks the current time in the scheduler
        int time = 0;

        System.out.println("\n--- Priority-Based Scheduling ---\n");

        ScheduledProcess current = null;

        // Continue until all processes are completed
        while (!processes.isEmpty() || !pq.isEmpty() || current != null) {
            // Add processes to the priority queue as they arrive at the current time
            for (Iterator<ScheduledProcess> it = processes.iterator(); it.hasNext();) {
                ScheduledProcess p = it.next();
                if (p.arrivalTime == time) {
                    // Add the process to the priority queue
                    pq.add(p);
                    // Remove the process from the list of unprocessed processes
                    it.remove();
                }
            }

            // Check for preemption: If a higher-priority process is available, preempt the current process
            if (current != null && !pq.isEmpty() && pq.peek().priority < current.priority) {
                System.out.println("Time " + time + ": Preempting " + current.command + " for " + pq.peek().command);

                // Add the current process back to the priority queue
                pq.add(current);

                // Reset the current process to allow the higher-priority process to execute
                current = null;
            }

            // Select the next process to execute from the priority queue
            if (current == null && !pq.isEmpty()) {
                // Retrieve and remove the highest-priority process from the queue
                current = pq.poll();

                // If this is the first time the process is being executed, set its start time
                if (current.startTime == -1) {
                    current.startTime = time;
                }
            }

            // Execute the current process for one unit of time
            if (current != null) {
                System.out.println("Time " + time + ": Executing " + current.command + " (PID: " + current.pid + ", Priority: " + current.priority + ")");

                // Simulate execution time
                Thread.sleep(500);
                // Decrease the remaining time of the current process
                current.remainingTime--;

                // Check if the process has completed execution
                if (current.remainingTime == 0) {
                    // If the process is completed, set its completion time
                    current.completionTime = time + 1;
                    System.out.println("Time " + (time + 1) + ": " + current.command + " completed.\n");
                    current = null;
                }
            } else {
                // If no process is ready to execute, the scheduler remains idle
                System.out.println("Time " + time + ": Idle");
                Thread.sleep(500);
            }

            // Increment the current time in the scheduler
            time++;
        }
    }
}
