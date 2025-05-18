import java.io.*;
import java.util.*;

public class Shell {
    private static Map<Integer, Process> backgroundJobs = new HashMap<>();
    private static Map<Integer, String> jobCommands = new HashMap<>();
    private static int jobCounter = 1;
    private static String currentDirectory = System.getProperty("user.dir");

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("shell> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            boolean runInBackground = input.endsWith("&");
            if (runInBackground) input = input.substring(0, input.length() - 1).trim();

            String[] tokens = input.split("\\s+");

            if (handleBuiltInCommands(tokens)) continue;

            runExternalCommand(tokens, runInBackground, input);
        }
    }

    public static void changeDirectory(String[] tokens) {
        if (tokens.length > 1) {
            File dir = new File(currentDirectory, tokens[1]);
            if (dir.exists() && dir.isDirectory()) {
                currentDirectory = dir.getAbsolutePath();

            } else {
                System.out.println("No such directory.");
            }
        } else {
            System.out.println("Usage: cd [directory]");
        }
    }

    public static void printDirectory() {
        System.out.println(currentDirectory);
    }

    public static void terminateShell() {
        System.out.println("Exiting shell...");
        for (Process process : backgroundJobs.values()) {
            process.destroy();
        }
        System.exit(0);
    }

    public static void echo(String[] tokens) {
        for (int i = 1; i < tokens.length; i++) {
            System.out.print(tokens[i] + " ");
        }
        System.out.println();
    }

    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void listFiles() {
        File current = new File(currentDirectory);
        for (String file : Objects.requireNonNull(current.list())) {
            System.out.println(file);
        }
    }

    private static void displayFileContents(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: cat [filename]");
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(tokens[1]))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("File not found.");
            }
        }
    }

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

    public static void listBackgroundJobs() {
        for (Map.Entry<Integer, Process> entry : backgroundJobs.entrySet()) {
            System.out.println("[" + entry.getKey() + "] PID: " + entry.getValue().pid() + " CMD: " + jobCommands.get(entry.getKey()));
        }
    }

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

    private static boolean handleBuiltInCommands(String[] tokens) {
        String command = tokens[0];
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

    private static void runExternalCommand(String[] tokens, boolean background, String fullCommand) {
        try {
            ProcessBuilder builder = new ProcessBuilder(tokens);
            builder.directory(new File(currentDirectory));
            if (!background) {
                Process process = builder.inheritIO().start();
                process.waitFor();
            } else {
                Process process = builder.start();
                backgroundJobs.put(jobCounter, process);
                jobCommands.put(jobCounter, fullCommand);
                System.out.println("Started background job [" + jobCounter + "] PID: " + process.pid());
                jobCounter++;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Command execution failed: " + e.getMessage());
        }
    }
}
