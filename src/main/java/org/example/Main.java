import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.util.regex.Pattern;

public class Main extends JFrame {
    String textPath = "";
    JPanel panel = new JPanel();
    Boolean readyState = false;
    JScrollPane scrollPane;
    int progress = 0;
    JProgressBar progressBar = new JProgressBar();
    JLabel title = new JLabel("SUPER JUICER DOWNLOAD MUSIC COVERS AND TAG NOW 100% SAFE");
    JButton startButton = new JButton("Set .txt File");
    static JTextArea outputArea = new JTextArea("this is bery bery bery safe no worries no virus malwar ur monies back granteed");
    public Main(){
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.add(panel);
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        scrollPane = new JScrollPane(outputArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret)outputArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.setBorder(BorderFactory.createEmptyBorder(25,10,20,10));
        startButton.setAlignmentX(CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setStringPainted(true);
        title.setFont(new Font("Verdana", Font.PLAIN, 14));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(startButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(scrollPane);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textPath.equals("")||readyState==false){
                    outputArea.setText(outputArea.getText()+"\n"+"txt path has not been set. Launching chooserPane");
                    System.out.println(".txt path has not been set. Launching chooserPane");
                    textPath = showFileChooser();
                    if(!textPath.equals("")){
                        showWarning("File has been set.\nMake sure you add a new line for each URL.\nOr else say bye bye to your system32");
                        readyState = true;
                        startButton.setText("Start Download");
                        outputArea.setText(outputArea.getText()+"\n"+"Ready to begin downloading. Press the button");
                        System.out.println("Ready to begin downloading. Press the button");
                    }
                }
                else{
                    Runnable runnable = () -> {
                        outputArea.setText("");
                        startButton.setEnabled(false);
                        downloadLoop();
                        startButton.setEnabled(true);

                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            }
        });
        this.setSize(550,300);

    }
    public static void main(String[] args) {
        new Main().setVisible(true);
    }
    private void downloadLoop(){
        ArrayList<String> songs = readFile(textPath);
        progress = 0;
        for(int i = 0;i<songs.size();i++) {
            try {
                deleteFiles("downloaded");
                downloadYouTube(songs.get(i));
                String info[] = parseJson(readJson(findJsonFile("downloaded"))); //title,uploader
                String uploader = info[1];
                String title = info[0];
                AudioFile f = AudioFileIO.read(findMP3File("downloaded"));
                Tag tag = f.getTag();
                tag.setField(FieldKey.ARTIST, uploader);
                tag.setField(FieldKey.TITLE, title);
                downloadImage("https://img.youtube.com/vi/"+info[2]+"/maxresdefault.jpg","img.jpg");
                Artwork cover = Artwork.createArtworkFromFile(new File("img.jpg"));
                tag.addField(cover);
                f.commit();
                clearThumbnail("img.jpg");
                moveFile(findMP3File("downloaded").getAbsolutePath(), "completed/" + removeNonAlphaNumeric(info[0]) + " ["+info[2]+ "].mp3");
                progress = i;
                System.out.println("Current Progress " + calculatePercentage(i+1,songs.size()));
                progressBar.setValue(calculatePercentage(i+1,songs.size()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private int calculatePercentage(int current, int total){
        double currentD = current;
        double totalD = total;
        return (int)((currentD/totalD)*100);
    }
    public static String showFileChooser() {
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
    public static void clearThumbnail(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    //download image using url
    public static void downloadImage(String url, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        URL urlObj = new URL(url);
        InputStream is = urlObj.openStream();
        byte[] b = new byte[2048];
        int length;
        while ((length = is.read(b)) != -1) {
            fos.write(b, 0, length);
        }
        fos.close();
        is.close();
    }

    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message, "JUST YOUR FRIENDLY NEIGHBORLY REMINDER", JOptionPane.WARNING_MESSAGE);
    }

    public static String removeNonAlphaNumeric(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }
    public static void moveFile(String source, String destination) {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);
        sourceFile.renameTo(destinationFile);
        outputArea.setText(outputArea.getText()+"\n"+"Moved file to Completed Folder");
        System.out.println("Moved file to Completed Folder");
    }

    public static File findMP3File(String directory){
        File dir = new File(directory);
        File[] files = dir.listFiles();
        for(File file : files){
            if(file.getName().endsWith(".mp3")){
                return file;
            }
        }
        return null;
    }

    public static String findJsonFile(String folderName) {
        File folder = new File(folderName);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().endsWith(".json")) {
                    return listOfFiles[i].getAbsolutePath();
                }
            }
        }
        return null;
    }
    public static String readJson(String fileName) {
        String json = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            json = sb.toString();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static String[] parseJson(String json) {
        String title = "";
        String uploader = "";
        String id = "";
        Pattern titlePattern = Pattern.compile("\"fulltitle\": \"(.*?)\",");
        Matcher titleMatcher = titlePattern.matcher(json);
        Pattern uploaderPattern = Pattern.compile("\"uploader\": \"(.*?)\",");
        Matcher uploaderMatcher = uploaderPattern.matcher(json);
        Pattern idPattern = Pattern.compile("\"id\": \"(.*?)\",");
        Matcher idMatcher = idPattern.matcher(json);
        titleMatcher.find();
        idMatcher.find();
        uploaderMatcher.find();
        title = titleMatcher.group(1);
        uploader = uploaderMatcher.group(1);
        id = idMatcher.group(1);
        String[] info = {title,uploader,id};
        return info;

    }
    public static void downloadYouTube(String url) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "yt-dlp.exe",
                    "--extract-audio",
                    "--audio-format", "mp3",
                    "--audio-quality", "0",
                    "--output", "downloaded/%(title)s_%(id)s.mp3",
                    "--ffmpeg-location","ffmpeg.exe",
                    "--write-info-json",
                    url
            );
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                outputArea.setText(outputArea.getText()+"\n"+line);
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void deleteFiles(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }
    public static ArrayList<String> readFile(String fileName) {
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

}
