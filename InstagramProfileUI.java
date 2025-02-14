import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;
import javax.swing.*;

public class InstagramProfileUI extends JFrame {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 500;
    private static final int PROFILE_IMAGE_SIZE = 80;
    private static final int GRID_IMAGE_SIZE = WIDTH / 3;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private User currentUser;

    public InstagramProfileUI(User user) {
        this.currentUser = user;
        loadUserData();
        initializeUI();
    }

    private void loadUserData() {
        currentUser.setPostCount(countUserImages());
        currentUser.setFollowersCount(countFollowers());
        currentUser.setFollowingCount(countFollowing());
        currentUser.setBio(fetchUserBio());
    }

    private int countUserImages() {
        Path imageDetailsFilePath = Paths.get("img", "image_details.txt");
        try (BufferedReader reader = Files.newBufferedReader(imageDetailsFilePath)) {
            return (int) reader.lines().filter(line -> line.contains("Username: " + currentUser.getUsername())).count();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int countFollowers() {
        return countUserConnections(false);
    }

    private int countFollowing() {
        return countUserConnections(true);
    }

    private int countUserConnections(boolean isFollowing) {
        Path followingFilePath = Paths.get("data", "following.txt");
        try (BufferedReader reader = Files.newBufferedReader(followingFilePath)) {
            return (int) reader.lines().map(line -> line.split(":"))
                .filter(parts -> parts.length == 2)
                .mapToInt(parts -> {
                    String[] connections = parts[1].split(";");
                    return (isFollowing ? parts[0].trim().equals(currentUser.getUsername()) :
                            Stream.of(connections).anyMatch(followingUser -> followingUser.trim().equals(currentUser.getUsername())))
                            ? connections.length : 0;
                }).sum();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String fetchUserBio() {
        Path bioDetailsFilePath = Paths.get("data", "credentials.txt");
        try (BufferedReader reader = Files.newBufferedReader(bioDetailsFilePath)) {
            return reader.lines()
                .filter(line -> line.startsWith(currentUser.getUsername() + ":"))
                .map(line -> line.split(":")[2])
                .findFirst().orElse("");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
