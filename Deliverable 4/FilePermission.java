import java.io.*;
import java.util.*;

public class FilePermission {
    private static final Map<String, Set<String>> filePermissions = new HashMap<>();

    public static void setPermission(String filename, String user, String permission) {
        filePermissions.computeIfAbsent(filename + ":" + user, k -> new HashSet<>()).add(permission);
    }

    public static boolean checkPermission(String filename, String user, String permission, String role) {
        if ("admin".equals(role)) return true;
        Set<String> perms = filePermissions.getOrDefault(filename + ":" + user, new HashSet<>());
        return perms.contains(permission);
    }

    // Example: default permission setup
    public static void setupDefaultPermissions() {
        try (BufferedReader reader = new BufferedReader(new FileReader("permissions.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                if (parts.length == 3) {
                    String filename = parts[0].trim();
                    String user = parts[1].trim();
                    String permission = parts[2].trim();
                    setPermission(filename, user, permission);
                } else {
                    System.out.println("Invalid entry: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading permissions file: " + e.getMessage());
        }
    }
}
