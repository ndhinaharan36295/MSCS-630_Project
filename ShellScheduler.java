import java.util.*;

class ScheduledProcess implements Comparable<ScheduledProcess> {
    int pid;                // Process ID
    int priority;           // Priority of the process (lower number means higher priority)
    String command;         // Command to execute
    int burstTime;          // Total time required for the process
    int remainingTime;      // Remaining time for the process
    int arrivalTime;        // Time at which the process arrives in the queue

    // Constructor to initialize a scheduled process
    public ScheduledProcess(int pid, int priority, String command, int burstTime, int arrivalTime) {
        this.pid = pid;
        this.priority = priority;
        this.command = command;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;  // Initially, remaining time is equal to burst time
        this.priority = priority;
        this.arrivalTime = arrivalTime;
    }

    // Method to compare processes based on priority (used for priority-based scheduling)
    @Override
    public int compareTo(ScheduledProcess o) {
        return Integer.compare(this.priority, o.priority);
    }
}

// This code implements a simple shell scheduler that can perform Round-Robin and Priority-Based scheduling.
public class ShellScheduler {
    private static int pidCounter = 1;

    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        List<ScheduledProcess> processList = new ArrayList<>();

        // Simulate input: Add processes to the list with predefined attributes
        processList.add(new ScheduledProcess(pidCounter++, 2, "Process A", 5, 0));
        processList.add(new ScheduledProcess(pidCounter++, 1, "Process B", 3, 0));
        processList.add(new ScheduledProcess(pidCounter++, 5, "Process C", 6, 3));
        processList.add(new ScheduledProcess(pidCounter++, 4, "Process D", 1, 5));
        processList.add(new ScheduledProcess(pidCounter++, 3, "Process E", 4, 6));

        // Command prompt for the user to choose a scheduling algorithm
        System.out.println("Choose scheduling algorithm: (1) Round-Robin (2) Priority-Based");
        int choice = scanner.nextInt();

        // Execute the chosen scheduling algorithm
        if (choice == 1) {
            System.out.println("Enter time slice (quantum): ");
            // Get the quantum time from user for Round-Robin scheduling
            int quantum = scanner.nextInt();
            roundRobinScheduling(new ArrayList<>(processList), quantum);
        } else {
            priorityBasedScheduling(new ArrayList<>(processList));
        }
    }

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
        // Priority queue to manage processes based on their priority
        PriorityQueue<ScheduledProcess> pq = new PriorityQueue<>();
        // Tracks the current time in the scheduler
        int time = 0;

        System.out.println("\n--- Priority-Based Scheduling ---\n");

        // Continue until all processes are completed
        while (!processes.isEmpty() || !pq.isEmpty()) {
            // Add processes to the priority queue based on their arrival time
            for (Iterator<ScheduledProcess> it = processes.iterator(); it.hasNext();) {
                ScheduledProcess p = it.next();

                if (p.arrivalTime <= time) {
                    pq.add(p);
                    it.remove();
                }
            }

            // Execute the process with the highest priority (lowest priority number)
            if (!pq.isEmpty()) {
                ScheduledProcess current = pq.poll();

                System.out.println("Time " + time + ": Executing " + current.command + " (PID: " + current.pid + ", Priority: " + current.priority + ") for " + current.burstTime + "s");

                // Simulate process execution
                Thread.sleep(current.burstTime * 500);

                // Update current time -- here, the entire process is completed (unlike, Round-Robin where we might have partial execution)
                time += current.burstTime;

                System.out.println("Time " + time + ": " + current.command + " completed.\n");
            } else {
                // If no process is ready at the current timestamp, the scheduler is idle
                System.out.println("Time " + time + ": Idle");
                Thread.sleep(500);
                time++;
            }
        }
    }
}
