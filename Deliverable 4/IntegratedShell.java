import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class IntegratedShell {
    private static String currentDirectory = System.getProperty("user.dir");

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        User currentUser = null;
        while (currentUser == null) {
            System.out.println("Please log in:");
            currentUser = UserAuthentication.authenticate(scanner);
        }

        FilePermission.setupDefaultPermissions();

        while (true) {
            System.out.println("\n=== Integrated Shell ===");
            System.out.println("1. Shell Commands");
            System.out.println("2. Scheduling Algorithms");
            System.out.println("3. Memory Management");
            System.out.println("4. Producer-Consumer Synchronization");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    shellCommands(scanner, currentUser);
                    break;
                case 2:
                    schedulingAlgorithms(scanner);
                    break;
                case 3:
                    memoryManagement(scanner);
                    break;
                case 4:
                    producerConsumerSynchronization();
                    break;
                case 5:
                    System.out.println("Exiting Integrated Shell...");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Shell Commands
    private static void shellCommands(Scanner scanner, User currentUser) throws IOException {
        ShellCommands shellCommands = new ShellCommands();
        
        while (true) {
            // Prompt for user input
            System.out.print("shell> ");
            // Read and trim input
            String input = scanner.nextLine().trim();
            // Skip if input is empty
            if (input.isEmpty()) continue;
            
            // Check if command should be run as a background job
            boolean runInBackground = input.endsWith("&");
            // Remove '&' if present in background job to properly parse commands
            if (runInBackground) input = input.substring(0, input.length() - 1).trim();

            // Handle built-in commands, if present in input
            if (shellCommands.handleBuiltInCommands(input, currentUser)) continue;
        }
    }

    // Scheduling Algorithms
    private static void schedulingAlgorithms(Scanner scanner) throws InterruptedException {
        List<ScheduledProcess> processList = Arrays.asList(
                new ScheduledProcess(1, 3, "Process A", 8, 0),
                new ScheduledProcess(2, 1, "Process B", 4, 2),
                new ScheduledProcess(3, 2, "Process C", 6, 4),
                new ScheduledProcess(4, 4, "Process D", 7, 6),
                new ScheduledProcess(5, 1, "Process E", 5, 7)
        );

        System.out.println("shell> Choose scheduling algorithm: (1) Round-Robin or (2) Priority-Based");
        int choice = scanner.nextInt();
        if (choice == 1) {
            System.out.println("shell> Enter time slice (quantum): ");
            int quantum = scanner.nextInt();
            ShellScheduler.roundRobinScheduling(new ArrayList<>(processList), quantum);
        } else {
            ShellScheduler.priorityBasedScheduling(new ArrayList<>(processList));
        }

        // calculate the performance metrics
        // waiting time, turnaround time, and response time of each processes
        int totalWaitingTime = 0, totalTurnAroundTime = 0, totalResponseTime = 0;

        for (ScheduledProcess p : processList) {
            int turnAroundTime = p.completionTime - p.arrivalTime;
            int waitingTime = turnAroundTime - p.burstTime;
            int responseTime = p.startTime - p.arrivalTime;

            System.out.println("Process ID: " + p.pid +
                    ", Turnaround Time: " + turnAroundTime +
                    ", Waiting Time: " + waitingTime +
                    ", Response Time: " + responseTime);

            totalWaitingTime += waitingTime;
            totalTurnAroundTime += turnAroundTime;
            totalResponseTime += responseTime;
        }

        // Calculate and print average metrics
        int n = processList.size();
        System.out.println("\nAverage Turnaround Time: " + (double) totalTurnAroundTime / n);
        System.out.println("Average Waiting Time: " + (double) totalWaitingTime / n);
        System.out.println("Average Response Time: " + (double) totalResponseTime / n);
    }

    // Memory Management
    private static void memoryManagement(Scanner scanner) {
        PagingSystem pagingSystem = new PagingSystem(3);

        System.out.println("shell> Choose memory management algorithm: (1) FIFO (2) LRU");
        int choice = scanner.nextInt();

        while (true) {
            // Prompt for user input
            System.out.print("shell> Input a page number to access from memory: ");

            // Read and trim input
            String pageInput = scanner.nextLine().trim();

            // Check if input is "exit"
            if (pageInput.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                break;
            }

            // Skip if input is empty
            if (pageInput.isEmpty()) continue;

            // Validate if input is numerical
            try {
                int pageNumber = Integer.parseInt(pageInput);

                // If successful, proceed with accessing the page

                // access page in the desired algorithm
                System.out.println("\nAccessing Page: " + pageNumber);
                if (choice == 1) {
                    pagingSystem.accessPageFIFO(pageNumber);
                    System.out.println("Current Memory: " + pagingSystem.fifoQueue);
                } else {
                    pagingSystem.accessPageLRU(pageNumber);
                    System.out.println("Current Memory: " + pagingSystem.memory.keySet());
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numerical page number to access or 'exit' to exit shell.");
            }
        }
    }

    // Producer-Consumer Synchronization
    private static void producerConsumerSynchronization() throws InterruptedException {
        BoundedBuffer buffer = new BoundedBuffer(5);

        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    buffer.produce(i);
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        });

        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    buffer.consume();
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {}
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}
