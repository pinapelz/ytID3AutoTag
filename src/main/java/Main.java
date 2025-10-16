import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import static UI.Modal.chooseBrowserType;
import static UI.Modal.showTextFileChooser;


public class Main extends JFrame {
    private static String completedDir;


    String textPath = "";

    static JTextArea outputArea = new JTextArea("");
    JPanel panel = new JPanel();
    JScrollPane scrollPane;
    JButton songsGen = new JButton("Generate text file");
    JButton editButton = new JButton("Edit Tags");
    JButton startButton = new JButton("Set .txt File");
    JButton configureDownloadButton = new JButton("Configure Download File Interactively");
    JButton setOutputDirButton = new JButton("Set MP3 Output Directory");
    JCheckBox defaultFileBox = new JCheckBox("Use location of last file");
    JProgressBar progressBar = new JProgressBar();
    JLabel title = new JLabel("YouTube to MP3 Auto Tagging [v1.5]");
    Boolean readyState = false;
    Configuration config = new Configuration();
    HashMap<String, String> configuration;

    public Main() {
        initializeComponents();
        initializeActionsListeners();
        config.createConfigurationFile();
        configuration = config.readConfigurationData();
        if(configuration.containsKey("outputPath") && !configuration.get("outputPath").isEmpty()){
            completedDir = configuration.get("outputPath");
        }
        else{
            createDefaultCompletedDirectories();
            completedDir = System.getProperty("user.dir") + "/completed";
        }
        outputArea.setText(outputArea.getText() + "\nOutput Directory set as: " + completedDir);

    }

    public static void main(String[] args) {
        // Launch GUI when no args provided
        if(args.length == 0) {
            FlatIntelliJLaf.setup();
            new Main().setVisible(true);
        }
        //TODO: Pass to Command Handler to run job otherwise...
    }


    /**
     * Calculate the percentage for progress bar
     * @param current The current number of songs downloaded
     * @param total The total number of songs to download
     * @return The percentage of songs downloaded
     */
    private int calculatePercentage(int current, int total) {
        return (int) (((double) current / (double) total) * 100);
    }

    public void downloadAndTag() {
        ArrayList<String> songs = FileUtility.txtToList(textPath);
        String browser = configuration.get("browser");
        int totalSongs = songs.size();
        int songsProcessed = 0;

        for (String line : songs) {
            System.out.println(line);
            Downloader downloader = new Downloader(completedDir, outputArea);
            boolean success = false;

            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    if (line.contains(",")) {
                        String[] parts = line.split(",");
                        String url = parts[0];
                        String stamp = parts[1];

                        if (downloader.download(url, stamp, browser)) {
                            success = true;
                            break;
                        } else {
                            System.out.println("Attempt " + attempt + " failed for " + url);
                        }
                    } else {
                        if (downloader.download(line, browser)) {
                            success = true;
                            break;
                        } else {
                            System.out.println("Attempt " + attempt + " failed for " + line);
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Error on attempt " + attempt + " for line: " + line + " -> " + e);
                }

                // wait before retrying (except after last attempt)
                if (attempt < 3) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {}
                }
            }

            if (!success) {
                UI.Modal.showError("Failed to download after 3 attempts: " + line);
            }

            songsProcessed++;
            progressBar.setValue(calculatePercentage(songsProcessed, totalSongs));
        }
    }


    /**
     * Initialize all GUI components
     */
    private void initializeComponents() {//Initiate GUI components
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setIconImage(new ImageIcon("icon.png").getImage());
        this.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        scrollPane = new JScrollPane(outputArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outputArea.setEditable(true);
        outputArea.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) outputArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 10, 20, 10));
        startButton.setAlignmentX(CENTER_ALIGNMENT);
        startButton.setSize(new Dimension(300, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        defaultFileBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        setOutputDirButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        songsGen.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Verdana", Font.PLAIN, 12));
        title.setFont(new Font("Verdana", Font.BOLD, 16));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(startButton);
        panel.add(defaultFileBox);
        panel.add(setOutputDirButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(5));
        panel.add(editButton);
        panel.add(Box.createVerticalStrut(5));
        outputArea.setEditable(false);
        configureDownloadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(configureDownloadButton);
        this.setSize(550, 450);
        this.setTitle("YTMP3Tagger");
    }



    /**
     * Initialize all action listeners for buttons
     */
    private void initializeActionsListeners() { //Add all actionlisteners for buttons
        defaultFileBox.addActionListener(e -> useLastInputTextFileLocation());
        startButton.addActionListener(e -> startDownloadTagJobs());
        editButton.addActionListener(e -> new TagEditorScreen().setVisible(true));
        configureDownloadButton.addActionListener(e -> new DownloadConfigPane().setVisible(true));
        setOutputDirButton.addActionListener(e -> chooseOutputDirectory());
    }

    private void writeFileContentsToOutputArea(String path){
        File file = new File(path);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                outputArea.setText(outputArea.getText() + "\n" + line);
            }
        } catch (FileNotFoundException e) {
            UI.Modal.showError("Unable to display contents of input file? Did you delete it?");
        }

    }

    /**
     * Create the directories for completed files
     */
    public void createDefaultCompletedDirectories(){
        Path completedDirPath = Paths.get( System.getProperty("user.dir") + "/completed");
        try {
            Files.createDirectories(completedDirPath);
        } catch (IOException e) {
            UI.Modal.showError("Unable to create directories for completed files");
        }
    }

    /**
     * Set the text file input path to the location that was used last time
     */
    private void useLastInputTextFileLocation(){
        if (defaultFileBox.isSelected()) {
            File file = new File(configuration.get("lastFile"));
            if (!file.exists()) {
                defaultFileBox.setSelected(false);
                UI.Modal.showError("Unable to find the location of your previous input file " +
                        "(Is this your first time using the app?)" +
                        "\nPlease select a new file using the \"Set .txt File\" button");
                return;
            }
            textPath = configuration.get("lastFile");
            readyState = true;
            startButton.setText("Start Download");
            outputArea.setText(outputArea.getText() + "\n" + "Ready to begin downloading. Press the button");
            writeFileContentsToOutputArea(configuration.get("lastFile"));
            System.out.println("Ready to begin downloading. Press the button");
        } else {
            readyState = false;
            startButton.setText("Set .txt File");
            textPath = "";

        }
    }

    /**
     * Deletes any possible remaining files from previous jobs
     */
    private void cleanRemainingFiles(){
        FileUtility.deleteALlFileOfType(System.getProperty("user.dir"), "webm");
        FileUtility.deleteALlFileOfType(System.getProperty("user.dir"), "json");
        FileUtility.deleteALlFileOfType(System.getProperty("user.dir"), "mp3");
    }

    /**
     * Starts the download and tagging process
     */
    private void startDownloadTagJobs(){
        cleanRemainingFiles();
        if (!readyState) {
            outputArea.setText(outputArea.getText() + "\n" + "txt path has not been set. Launching chooserPane");
            System.out.println(".txt path has not been set. Launching chooserPane");
            String path = showTextFileChooser();
            textPath = path;
            if(path == null){
                UI.Modal.showWarning("No text file was selected. Aborting operation");
                return;
            }
            config.modifyConfigurationValue("lastFile", path);
            configuration = config.readConfigurationData();

            try {
                if (!textPath.isEmpty()) {
                    UI.Modal.showWarning("File has been set.\nMake sure you add a new line for each URL");
                    readyState = true;
                    writeFileContentsToOutputArea(textPath);
                    startButton.setText("Start Download");
                    outputArea.setText(outputArea.getText() + "\n" + "Ready to begin downloading. Press the button");
                    System.out.println("Ready to begin downloading. Press the button");
                }
            } catch (Exception ex) {

            }
        } else {
            if(!configuration.containsKey("browser") || configuration.get("browser").isEmpty()){
                System.out.println("Browser not set, this is needed to read cookies");
                String browser = chooseBrowserType();
                if(browser.isEmpty()){
                    return;
                }
                config.modifyConfigurationValue("browser", browser);
                configuration = config.readConfigurationData();
            }
            outputArea.setText(outputArea.getText() + "\n\n" + "Files will be saved to: " + completedDir);
            Runnable runnable = () -> {
                outputArea.setText("");
                startButton.setEnabled(false);
                downloadAndTag();
                startButton.setEnabled(true);

            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    public void chooseOutputDirectory(){
        completedDir = UI.Modal.showDirectoryChooser(configuration.get("outputPath"));
        if (completedDir == null) {
            outputArea.setText(outputArea.getText() + "\n" + "No directory was selected. No changes were made.");
        }
        else{
            config.modifyConfigurationValue("outputPath", completedDir);
            configuration = config.readConfigurationData();
            outputArea.setText(outputArea.getText() + "\n" + "Output directory set as: " + completedDir);
        }
    }


}
