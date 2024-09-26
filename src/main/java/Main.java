import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;


public class Main extends JFrame {
    final static String BLACKLIST = "blacklist.txt";
    private static String COMPLETED_DIR = "completed";


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
    JCheckBox useBlacklistBox = new JCheckBox("Use Blacklist.txt");
    JProgressBar progressBar = new JProgressBar();
    JLabel title = new JLabel("YouTube to MP3 Auto Tagging [CrossPlatform]");
    Boolean useBlacklist = false;
    Boolean readyState = false;

    public Main() {
        initializeComponents();
        initializeActionsListeners();
        createDirectories();
    }

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        new Main().setVisible(true);
    }


    public static ArrayList<String> txtToList(String fileName) {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * Calculate the percentage for progress bar
     * @param current The current number of songs downloaded
     * @param total The total number of songs to download
     * @return The percentage of songs downloaded
     */
    private int calculatePercentage(int current, int total) {//Calculate the percentage when give numerator and denominator
        double currentD = current;
        double totalD = total;
        return (int) ((currentD / totalD) * 100);
    }

    public void downloadAndTag(){
        ArrayList<String> songs = txtToList(textPath);
        int totalSongs = songs.size();
        int songsProcessed = 0;
        for(String line: songs){
            System.out.println(line);
            if(line.contains(",")){
                String[] parts = line.split(",");
                String url = parts[0];
                String stamp = parts[1];
                Downloader downloader = new Downloader(COMPLETED_DIR, outputArea);
                downloader.download(url, stamp);
            }
            else{
                Downloader downloader = new Downloader(COMPLETED_DIR, outputArea);
                downloader.download(line);
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
        useBlacklistBox.setAlignmentX(Component.CENTER_ALIGNMENT);
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
        panel.add(useBlacklistBox);
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


    public static String showTextFileChooser() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text File", "txt", "text");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Select a text file");
        chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Initialize all action listeners for buttons
     */
    private void initializeActionsListeners() { //Add all actionlisteners for buttons
        defaultFileBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (defaultFileBox.isSelected()) {
                    File file = new File("lastFile.txt");
                    if (!file.exists()) {
                        defaultFileBox.setSelected(false);
                        JOptionPane.showMessageDialog(null, "Unable to find the location of your previous file, please select a new one");
                        return;
                    }
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new FileReader(file));
                        String line = br.readLine();
                        if (line == null) {
                            defaultFileBox.setSelected(false);
                            JOptionPane.showMessageDialog(null, "Unable to find the location of your previous file, please select a new one");
                            return;
                        }
                        textPath = line;
                        COMPLETED_DIR = textPath.substring(0, textPath.lastIndexOf(File.separator));
                        readyState = true;
                        startButton.setText("Start Download");
                        outputArea.setText(outputArea.getText() + "\n" + "Ready to begin downloading. Press the button");
                        writeFileContentsToOutputArea(textPath);
                        System.out.println("Ready to begin downloading. Press the button");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    readyState = false;
                    startButton.setText("Set .txt File");
                    textPath = "";

                }

            }
        });
        useBlacklistBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (useBlacklistBox.isSelected()) {
                    useBlacklist = true;
                } else {
                    useBlacklist = false;
                }

            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileUtility fileUtility = new FileUtility();
                fileUtility.deleteALlFileOfType(System.getProperty("user.dir"), "webm");
                fileUtility.deleteALlFileOfType(System.getProperty("user.dir"), "json");
                fileUtility.deleteALlFileOfType(System.getProperty("user.dir"), "mp3");
                if (readyState == false) {
                    outputArea.setText(outputArea.getText() + "\n" + "txt path has not been set. Launching chooserPane");
                    System.out.println(".txt path has not been set. Launching chooserPane");
                    String path = showTextFileChooser();
                    textPath = path;
                    COMPLETED_DIR = path.substring(0, path.lastIndexOf(File.separator));
                    try {
                        if (!textPath.equals("")) {
                            showWarning("File has been set.\nMake sure you add a new line for each URL");
                            readyState = true;
                            writeFileContentsToOutputArea(textPath);
                            startButton.setText("Start Download");
                            outputArea.setText(outputArea.getText() + "\n" + "Ready to begin downloading. Press the button");
                            File file = new File("lastFile.txt");
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            FileWriter fw = new FileWriter(file);
                            fw.write(textPath);
                            fw.close();


                            System.out.println("Ready to begin downloading. Press the button");
                        }
                    } catch (Exception ex) {

                    }
                } else {
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
        });
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TagEditorScreen().setVisible(true);
            }
        });
        configureDownloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DownloadConfigPane().setVisible(true);
            }
        });
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
            e.printStackTrace();
        }

    }

    /**
     * Create the directories for the downloaded and completed files
     */
    public void createDirectories(){
        File f2 = new File(COMPLETED_DIR);
        if (!f2.exists()) {
            f2.mkdir();
        }
    }

    /**
     * Show warning message
     */
    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message, "JUST YOUR FRIENDLY NEIGHBORLY REMINDER", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Show error message
     */
    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Convert timestamp to seconds hh:mm:ss or mm:ss
     * @param timestamp The timestamp to convert
     *                  Example: 01:03:20
     * @return The total number of seconds
     */
    public static int timestampToSeconds(String timestamp){
        int totalSeconds = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date date = sdf.parse(timestamp);
            int hours = date.getHours();
            int minutes = date.getMinutes();
            int seconds = date.getSeconds();
            totalSeconds = hours * 3600 + minutes * 60 + seconds;
            System.out.println(totalSeconds);
        }
        catch (Exception e){
            System.out.println("Error converting timestamp to seconds");
            e.printStackTrace();
        }
        return totalSeconds;

    }


}
