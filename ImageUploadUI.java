import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageUploadUI extends JFrame {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 500;
    private JLabel imagePreviewLabel;
    private JTextArea bioTextArea;
    private JButton uploadButton;
    private JButton saveButton;
    private final ImageManager imageManager;

    public ImageUploadUI() {
        this.imageManager = new ImageManager();
        setTitle("Upload Image");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        JPanel headerPanel = createHeaderPanel();
        JPanel contentPanel = createContentPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imagePreviewLabel.setPreferredSize(new Dimension(WIDTH, HEIGHT / 3));
        contentPanel.add(imagePreviewLabel);

        bioTextArea = new JTextArea("Enter a caption");
        bioTextArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        JScrollPane bioScrollPane = new JScrollPane(bioTextArea);
        bioScrollPane.setPreferredSize(new Dimension(WIDTH - 50, HEIGHT / 6));
        contentPanel.add(bioScrollPane);

        uploadButton = new JButton("Upload Image");
        uploadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadButton.addActionListener(this::uploadAction);
        contentPanel.add(uploadButton);

        saveButton = new JButton("Save Caption");
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.addActionListener(this::saveBioAction);
        contentPanel.add(saveButton);

        return contentPanel;
    }

    private void uploadAction(ActionEvent event) {
        File selectedFile = imageManager.selectImageFile();
        if (selectedFile != null) {
            imageManager.processImageUpload(selectedFile, bioTextArea.getText(), imagePreviewLabel);
            uploadButton.setText("Upload Another Image");
        }
    }

    private void saveBioAction(ActionEvent event) {
        JOptionPane.showMessageDialog(this, "Caption saved: " + bioTextArea.getText());
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(51, 51, 51));
        JLabel lblTitle = new JLabel(" Upload Image 🐥");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        return headerPanel;
    }
}

class ImageManager {
    public File selectImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an image file");
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg");
        fileChooser.addChoosableFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(null);
        return (returnValue == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
    }

    public void processImageUpload(File selectedFile, String bio, JLabel imagePreviewLabel) {
        try {
            String username = "default_user";
            int imageId = getNextImageId(username);
            String fileExtension = getFileExtension(selectedFile);
            String newFileName = username + "_" + imageId + "." + fileExtension;

            Path destPath = Paths.get("img", "uploaded", newFileName);
            Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

            saveImageInfo(username + "_" + imageId, username, bio);
            imagePreviewLabel.setIcon(new ImageIcon(destPath.toString()));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error saving image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getNextImageId(String username) throws IOException {
        Path storageDir = Paths.get("img", "uploaded");
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        return (int) Files.list(storageDir)
                .filter(path -> path.getFileName().toString().startsWith(username + "_"))
                .count() + 1;
    }

    private void saveImageInfo(String imageId, String username, String bio) throws IOException {
        Path infoFilePath = Paths.get("img", "image_details.txt");
        if (!Files.exists(infoFilePath)) {
            Files.createFile(infoFilePath);
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Files.write(infoFilePath, 
                ("ImageID: " + imageId + ", Username: " + username + ", Bio: " + bio + ", Timestamp: " + timestamp + ", Likes: 0\n").getBytes(), 
                StandardOpenOption.APPEND);
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        return (lastIndexOf == -1) ? "" : name.substring(lastIndexOf + 1);
    }
}
