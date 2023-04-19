import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import javax.swing.*;
import javax.swing.text.DefaultCaret;


public class Main extends JFrame {
    final static String BLACKLIST = "blacklist.txt";
    final static String DOWNLOADED_DIR = "downloaded";
    final static String COMPLETED_DIR = "completed";


    String textPath = "";
    String formats[] = {"maxresdefault.jpg", "mqdefault.jpg", "hqdefault.jpg"};

    static JTextArea outputArea = new JTextArea("");
    JPanel panel = new JPanel();
    JScrollPane scrollPane;
    JButton songsGen = new JButton("Generate text file");
    JButton editButton = new JButton("Edit Tags");
    JButton startButton = new JButton("Set .txt File");
    JCheckBox defaultFileBox = new JCheckBox("Use Default songs.txt file");
    JCheckBox useBlacklistBox = new JCheckBox("Use Blacklist.txt");
    JProgressBar progressBar = new JProgressBar();
    JLabel title = new JLabel("YouTube to MP3 Auto Tagging [1]");
    Boolean useBlacklist = false;
    Boolean readyState = false;
    Boolean useDefault = false;
    FileUtility fileUtil = new FileUtility();

    public Main() {
        initializeComponents();
        initializeActionsListeners();
        createDirectories();
    }

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        new Main().setVisible(true);
    }

    /**
     * Download and tag all songs in the text file
     */
    private void downloadAndTag() {
        ArrayList<String> songs = fileUtil.txtToArrayList(textPath);
        String timeAppend = "";
        boolean partFlag = false;
        for (int i = 0; i < songs.size(); i++) {
            try {
                fileUtil.deleteAllFilesDir(DOWNLOADED_DIR);

                //Check if user's URL wants to download a part or full audio based on commas
                ArrayList<String> splitStamp = new ArrayList<>(Arrays.asList(songs.get(i).split(",")));
                switch(splitStamp.size()){
                    case 1:
                        downloadContentFull(songs.get(i));
                        partFlag = false;
                        break;
                    case 2:
                        timeAppend = downloadContentPartial(splitStamp.get(0), splitStamp.get(1));
                        partFlag = true;
                        break;

                    default:
                        showError("Invalid Input: " + songs.get(i)+
                                  "\nReason: Invalid formatting. Please use the format: URL,START_TIME:END_TIME");
                        return;
                }


                String info[] = fileUtil.parseInfoJSON(fileUtil.jsonToString(fileUtil.findJsonFile(DOWNLOADED_DIR))); //title,uploader
                String uploader = info[1];
                String title = info[0];
                String urlID = info[2];
                String imageUrl = "https://img.youtube.com/vi/" + urlID + "/";

                // Remove blacklisted words if asked to
                if (useBlacklist) {
                    System.out.println("Using blacklist. Removing blacklisted words from title and uploader");
                    uploader = fileUtil.removeBlacklist(uploader, BLACKLIST);
                    title = fileUtil.removeBlacklist(title, BLACKLIST);
                }

                // Method downloads as MP4, then converts to MP3. It's faster
                File mp4File = fileUtil.findFileType(DOWNLOADED_DIR ,"mp4");
                mp4Tomp3(mp4File);

                boolean taggingSuccessful = tagMp3InDir(uploader, title, imageUrl);
                if(!taggingSuccessful)
                    return;

                // If user wants to download a part of the video, append the time to the title. Else just move the file
                File mp3Path = fileUtil.findFileType(DOWNLOADED_DIR, "mp3");
                String destinationPath = partFlag ? COMPLETED_DIR+"/" + fileUtil.removeNonAlphaNumeric(title) + "[" + urlID + "]" + timeAppend + ".mp3" :
                        COMPLETED_DIR+"/" + fileUtil.removeNonAlphaNumeric(info[0]) + "[" + urlID + "].mp3";
                System.out.println(destinationPath);
                fileUtil.moveFile(mp3Path.getAbsolutePath(), destinationPath);

                outputArea.setText(outputArea.getText() + "\n" + "Moved file to Completed Folder");
                System.out.println("Current Progress " + calculatePercentage(i + 1, songs.size()));
                progressBar.setValue(calculatePercentage(i + 1, songs.size()));
            } catch (Exception e) {
                showError("Error occured while downloading and tagging. Check the logs for more info");
                e.printStackTrace();
            }
        }
    }

    /**
     * Tag mp3 with title, uploader, and image
     * @param uploader Uploader of the video
     * @param title Title of the video
     * @param imageUrl URL of to the thumbnail image
     */
    public boolean tagMp3InDir(String uploader, String title, String imageUrl) {//Tag mp3 file in downloaded directory
        try {
            AudioFile f = AudioFileIO.read(fileUtil.findFileType(DOWNLOADED_DIR, "mp3"));
            Tag tag = f.getTag();
            System.out.println("Uploader: " + uploader);
            System.out.println("Title: " + title);
            tag.setField(FieldKey.ARTIST, uploader);
            tag.setField(FieldKey.TITLE, title);
            fileUtil.downloadImage(imageUrl, "img.jpg", formats);
            Artwork cover = Artwork.createArtworkFromFile(new File("img.jpg"));
            tag.addField(cover);
            f.commit();
            fileUtil.deleteFile("img.jpg");
        }
        catch(Exception e){
            showError("Error occured while tagging mp3. Check your program version");
            return false;
        }
        return true;

    }

    /**
     * Download part of YouTube URL in MP3 format
     * @param url Youtube URL
     */
    public static void downloadContentFull(String url) {//Download mp3 of youtube video using yt-dlp.exe. Ran from cmd
        try {

            ProcessBuilder builder = new ProcessBuilder(
                    "yt-dlp.exe",
                    "-vU",
                    "--extract-audio",
                    "--audio-format", "mp3",
                    "--audio-quality", "0",
                    "--output", DOWNLOADED_DIR+"/%(title)s_%(id)s.mp3",
                    "--ffmpeg-location", "ffmpeg.exe",
                    "--write-info-json",
                    url
            );
            builder.redirectErrorStream(true);
            Process p = builder.start();
            relayConsole(p);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An Error occured while downloading using" +
                    " yt-dlp", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    /**
     * Download part of YouTube URL in MP4 format
     * @param url Youtube URL
     * @param stamp Time stamp in format HH:MM:SS-HH:MM:SS
     * @return String of time stamp to be used on filename startTimeInSeconds to endTimeInSeconds
     */
    public static String downloadContentPartial(String url, String stamp) { //Download mp3 of youtube video using yt-dlp.exe. Ran from cmd
        System.out.println(url + " " + stamp);
        ArrayList<String> times = new ArrayList<>(Arrays.asList(stamp.split("-")));
        String startTime = times.get(0);
        String endTime = times.get(1);

        // Time to start in seconds and time to end in seconds
        int startSec = timestampToSeconds(startTime);
        int endSec = timestampToSeconds(endTime);
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "yt-dlp.exe",
                    "-vU",
                    "-f","\"(bestvideo+bestaudio/best)[protocol!*=dash]\"",
                    "--external-downloader", "ffmpeg.exe",
                    "--external-downloader-args", "\"ffmpeg_i:-ss " + startSec + " -to " + endSec + "\"",
                    "--output", "downloaded/%(title)s_%(id)s.mp4",
                    "--write-info-json",
                    url
            );
            builder.redirectErrorStream(true);
            Process p = builder.start();
            relayConsole(p);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An Error occured while downloading using" +
                    " yt-dlp", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return startSec + "to" + endSec;
    }

    /**
     * Initialize all GUI components
     */
    private void initializeComponents() {//Initiate GUI components
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
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
        panel.add(Box.createVerticalStrut(8));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(5));
        panel.add(editButton);
        panel.add(useBlacklistBox);
        panel.add(Box.createVerticalStrut(8));
        this.setSize(550, 450);
        this.setTitle("YTMP3Tagger");

    }

    /**
     * Initialize all action listeners for buttons
     */
    private void initializeActionsListeners() { //Add all actionlisteners for buttons
        defaultFileBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = new File("songs.txt");
                if (f.exists() & !f.isDirectory() && !useDefault) {
                    System.out.println("songs found");
                    textPath = "songs.txt";
                    showWarning("Default File has been set.\nMake sure you add a new line for each URL");
                    readyState = true;
                    startButton.setText("Start Download");
                    outputArea.setText(outputArea.getText() + "\n" + "Ready to begin downloading. Press the button");
                    System.out.println("Ready to begin downloading. Press the button");
                    useDefault = true;

                } else {
                    useDefault = false;
                    readyState = false;
                    startButton.setText("Set .txt file");
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
                if (readyState == false) {
                    outputArea.setText(outputArea.getText() + "\n" + "txt path has not been set. Launching chooserPane");
                    System.out.println(".txt path has not been set. Launching chooserPane");
                    textPath = fileUtil.showTextFileChooser();
                    try {
                        if (!textPath.equals("")) {
                            showWarning("File has been set.\nMake sure you add a new line for each URL");
                            readyState = true;
                            startButton.setText("Start Download");
                            outputArea.setText(outputArea.getText() + "\n" + "Ready to begin downloading. Press the button");
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
    }

    /**
     * Convert mp4 to mp3 using ffmpeg
     * @param mp4File The mp4 file to convert
     */
    public static void mp4Tomp3(File mp4File){
        try {
            String mp4FileName = mp4File.getName();
            String mp3FileName = mp4FileName.substring(0, mp4FileName.length() - 4) + ".mp3";
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "ffmpeg -i \"" + mp4File.getAbsolutePath() + "\" \""+DOWNLOADED_DIR+"/" + mp3FileName+"\""
            );
            builder.redirectErrorStream(true);
            Process p = builder.start();
            relayConsole(p);
            p.waitFor();
            System.out.println("Conversion of MP4 to MP3 complete");

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Relays the console output from the CMD to the outputArea
     * @param p The process to relay to the outputArea from
     */
    public static void relayConsole(Process p) {
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String cmd_line;
        while (true) {
            try {
                cmd_line = r.readLine();
                if (cmd_line == null) {
                    break;
                }
                outputArea.setText(outputArea.getText() + "\n" + cmd_line);
                System.out.println(cmd_line);
            }
            catch (IOException e) {
                System.out.println("Error while relaying from CMD");
            }
        }
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

    /**
     * Create the directories for the downloaded and completed files
     */
    public void createDirectories(){
        File f = new File(DOWNLOADED_DIR);
        if (!f.exists()) {
            f.mkdir();
        }
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
