import java.io.*;
import java.util.*;

public class ShellCommands {
    // Maps to store background jobs and their corresponding commands
    private static Map<Integer, Process> backgroundJobs = new HashMap<>();
    private static Map<Integer, String> jobCommands = new HashMap<>();

    // Counter for job IDs
    private static int jobCounter = 1;
    
    // Current working directory
    private static String currentDirectory;

    public ShellCommands() {
        currentDirectory = System.getProperty("user.dir");
    }

    // method to handle Changing the current working directory
    public static void changeDirectory(String[] tokens) {
        if (tokens.length > 1) {
            File dir = new File(currentDirectory, tokens[1]);
            if (dir.exists() && dir.isDirectory()) {
                // Update current directory in memory -- so can used across the program
                currentDirectory = dir.getAbsolutePath();
            } else {
                System.out.println("No such directory.");
            }
        } else {
            System.out.println("Usage: cd [directory]");
        }
    }

    // method to handle Printing the current working directory
    public static void printDirectory() {
        System.out.println(currentDirectory);
    }

    // method to handle Terminating the shell and clean up background jobs
    public static void terminateShell() {
        System.out.println("Exiting shell...");
        for (Process process : backgroundJobs.values()) {
            process.destroy(); // Destroy all background processes
        }
        System.exit(0);  // Exit the program
    }

    // method to handle Echoing the input back to the user
    public static void echo(String[] tokens) {
        for (int i = 1; i < tokens.length; i++) {
            System.out.print(tokens[i] + " ");
        }
        System.out.println();
    }

    // method to handle Clearing the terminal screen
    public static void clear() {
        // ANSI escape code to clear screen
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // method to handle Listing files in the current directory
    public static void listFiles() {
        File current = new File(currentDirectory);
        for (String file : Objects.requireNonNull(current.list())) {
            System.out.println(file);
        }
    }

    // method to handle Displaying the contents of a file
    private static void displayFileContents(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: cat [filename]");
        } else {
            File file = new File(currentDirectory, tokens[1]); // Resolve relative to currentDirectory
            if (!file.exists() || !file.isFile()) {
                System.out.println("File not found.");
                return;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
    }

    // method to handle Creating a new directory
    private static void makeDirectory(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: mkdir [directory]");
        } else {
            File dir = new File(currentDirectory, tokens[1]);
            if (!dir.exists()) {
                dir.mkdir();
            } else {
                System.out.println("Directory already exists.");
            }
        }
    }

    // method to handle Removing a directory, if empty
    private static void removeDirectory(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: rmdir [directory]");
        } else {
            File dir = new File(currentDirectory, tokens[1]);
            if (dir.exists() && dir.isDirectory() && Objects.requireNonNull(dir.list()).length == 0) {
                dir.delete();
            } else {
                System.out.println("Directory not empty or doesn't exist.");
            }
        }
    }

    // method to handle Removing a file
    private static void removeFile(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: rm [filename]");
        } else {
            File file = new File(currentDirectory, tokens[1]);
            if (file.exists()) {
                file.delete();
            } else {
                System.out.println("File not found.");
            }
        }
    }

    // method to handle the Creation of a file or update it's timestamp (if already present)
    private static void touchFile(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: touch [filename]");
        } else {
            File file = new File(currentDirectory, tokens[1]);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    file.setLastModified(System.currentTimeMillis());
                }
            } catch (IOException e) {
                System.out.println("Error touching file.");
            }
        }
    }

    // method to handle Killing of a background process by its PID
    public static boolean killProcess(String[] tokens) {
        if (tokens.length > 1) {
            try {
                int pid = Integer.parseInt(tokens[1]);
                for (Map.Entry<Integer, Process> entry : backgroundJobs.entrySet()) {
                    if (entry.getValue().pid() == pid) {
                        entry.getValue().destroy();
                        backgroundJobs.remove(entry.getKey());
                        jobCommands.remove(entry.getKey());
                        System.out.println("Process " + pid + " killed.");
                        return true;
                    }
                }
                System.out.println("No such process.");
                return false;
            } catch (NumberFormatException e) {
                System.out.println("Invalid PID.");
                return false;
            }
        } else {
            System.out.println("Usage: kill [pid]");
            return false;
        }
    }

    // method to handle the Listing all background jobs
    public static void listBackgroundJobs() {
        for (Map.Entry<Integer, Process> entry : backgroundJobs.entrySet()) {
            System.out.println("[" + entry.getKey() + "] PID: " + entry.getValue().pid() + " CMD: " + jobCommands.get(entry.getKey()));
        }
    }

    // Bring a background job to the foreground
    public static void bringToForeground(String[] tokens) {
        if (tokens.length > 1) {
            try {
                int jobId = Integer.parseInt(tokens[1]);
                Process p = backgroundJobs.get(jobId);
                if (p != null) {
                    p.waitFor();
                    backgroundJobs.remove(jobId);
                    jobCommands.remove(jobId);
                } else {
                    System.out.println("No such job.");
                }
            } catch (Exception e) {
                System.out.println("Invalid job ID.");
            }
        } else {
            System.out.println("Usage: fg [job_id]");
        }
    }

    private static boolean handlePipedCommands(String input) {
        try {
            String[] commands = input.split("\\|");
            List<Process> processes = new ArrayList<>();

            ProcessBuilder firstBuilder = new ProcessBuilder(commands[0].trim().split("\\s+"));
            Process first = firstBuilder.start();
            processes.add(first);

            Process previous = first;

            for (int i = 1; i < commands.length; i++) {
                String[] parts = commands[i].trim().split("\\s+");

                ProcessBuilder builder = new ProcessBuilder(parts);
                Process current = builder.start();

                // chain previous output to current input
                chainResults(previous.getInputStream(), current.getOutputStream());

                processes.add(current);
                previous = current;
            }

            // Output from the last command goes to the shell
            try (Scanner sc = new Scanner(previous.getInputStream())) {
                while (sc.hasNextLine()) {
                    System.out.println(sc.nextLine());
                }
            }

            // Wait for all processes to complete
            for (Process p : processes) {
                p.waitFor();
            }
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing piped command: " + e.getMessage());
        }
        
        return true;
    }

    private static void chainResults(InputStream src, OutputStream dest) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(src));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dest))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static boolean validateUserPermission(String command, String[] tokens, User currentUser) {
        if (List.of("cat", "rm", "touch").contains(command)) {
            String filename = (tokens.length > 1) ? tokens[1] : null;

            String permission = switch (command) {
                case "cat" -> "read";
                case "rm" -> "write";
                case "touch" -> "write";
                default -> "";
            };

            if (filename != null && !FilePermission.checkPermission(filename, currentUser.username, permission, currentUser.role)) {
                System.out.println("Permission denied: You lack '" + permission + "' access to " + filename);
                return false;
            }
        }
        System.out.println("Permission granted for user: " + currentUser.username + " as an " + currentUser.role);
        return true;
    }

    // method to Handle all the built-in shell commands
    public static boolean handleBuiltInCommands(String input, User currentUser) {
        if (input.contains("|")) {
            return handlePipedCommands(input);
        } else {
            String[] tokens = input.split("\\s+");
            String command = tokens[0];

            // Check permission for file-modifying commands
            if (!validateUserPermission(command, tokens, currentUser)) {
                return false;
            }
            else {
                switch (command) {
                    case "cd":
                        changeDirectory(tokens);
                        return true;
                    case "pwd":
                        printDirectory();
                        return true;
                    case "exit":
                        terminateShell();
                    case "echo":
                        echo(tokens);
                        return true;
                    case "clear":
                        clear();
                        return true;
                    case "ls":
                        listFiles();
                        return true;
                    case "cat":
                        displayFileContents(tokens);
                        return true;
                    case "mkdir":
                        makeDirectory(tokens);
                        return true;
                    case "rmdir":
                        removeDirectory(tokens);
                        return true;
                    case "rm":
                        removeFile(tokens);
                        return true;
                    case "touch":
                        touchFile(tokens);
                        return true;
                    case "kill":
                        killProcess(tokens);
                    case "jobs":
                        listBackgroundJobs();
                        return true;
                    case "fg":
                        bringToForeground(tokens);
                        return true;
                    case "bg":
                        System.out.println("Background processes are already running.");
                        return true;
                    default:
                        return false;
                }
            }
        }
    }

    // Run an external command
    public static void runExternalCommand(boolean background, String input) {
        String[] tokens = input.split("\\s+");

        try {
            // Create a process builder
            ProcessBuilder builder = new ProcessBuilder(tokens);
            builder.directory(new File(currentDirectory));
            if (!background) {
                // Run in foreground
                Process process = builder.inheritIO().start();
                process.waitFor();   // Wait for the process to complete
            } else {
                // Run in background
                Process process = builder.start();
                backgroundJobs.put(jobCounter, process);  // Add to background jobs
                jobCommands.put(jobCounter, input);  // Store the command
                System.out.println("Started background job [" + jobCounter + "] PID: " + process.pid());
                jobCounter++;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Command execution failed: " + e.getMessage());
        }
    }
}
