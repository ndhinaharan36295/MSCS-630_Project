import java.io.*;
import java.util.*;

class User {
    public final String username;
    public final String role;

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }
}

public class UserAuthentication {
    private static final Map<String, String[]> userDB = new HashMap<>();

    static {
        loadUsers();
    }

    public static User authenticate(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (userDB.containsKey(username)) {
            String[] info = userDB.get(username);
            if (info[0].equals(password)) {
                System.out.println("\nLogin successful. Role: " + info[1]);
                return new User(username, info[1]);
            }
        }

        System.out.println("Invalid credentials.");
        return null;
    }

    private static void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.strip().split(":");
                if (parts.length == 3) {
                    userDB.put(parts[0], new String[]{parts[1], parts[2]});
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load user credentials: " + e.getMessage());
        }
    }
}
