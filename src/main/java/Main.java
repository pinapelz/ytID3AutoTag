import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class Main extends JFrame {
    String textPath = "";
    JPanel panel = new JPanel();
    Boolean readyState = false;
    JScrollPane scrollPane;
    JButton editButton = new JButton("Edit Tags");
    int progress = 0;
    FileUtility fileUtil = new FileUtility();
    JProgressBar progressBar = new JProgressBar();
    JLabel title = new JLabel("SUPER JUICER DOWNLOAD MUSIC COVERS AND TAG NOW 100% SAFE");
    JButton startButton = new JButton("Set .txt File");
    static JTextArea outputArea = new JTextArea("this is bery bery bery safe no worries no virus malwar ur monies back granteed");

    public Main(){
        initializeComponents();
        initializeActionsListeners();
    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }

    private void initializeComponents(){
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
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setStringPainted(true);
        title.setFont(new Font("Verdana", Font.PLAIN, 14));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(startButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(5));
        panel.add(editButton);
        this.setSize(550,300);
    }

    private void initializeActionsListeners(){
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textPath.equals("")||readyState==false){
                    outputArea.setText(outputArea.getText()+"\n"+"txt path has not been set. Launching chooserPane");
                    System.out.println(".txt path has not been set. Launching chooserPane");
                    textPath = fileUtil.showTextFileChooser();
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
            public void actionPerformed(ActionEvent e){

            }
        });
    }

    private void downloadAndTag(){
        ArrayList<String> songs = fileUtil.txtToArrayList(textPath);
        progress = 0;
        for(int i = 0;i<songs.size();i++) {
            try {
                fileUtil.deleteAllFilesDir("downloaded");
                youtubeToMP3(songs.get(i));
                String info[] = fileUtil.parseJson(fileUtil.jsonToString(fileUtil.findJsonFile("downloaded"))); //title,uploader
                String uploader = info[1];
                String title = info[0];
                AudioFile f = AudioFileIO.read(fileUtil.findMP3File("downloaded"));
                Tag tag = f.getTag();
                tag.setField(FieldKey.ARTIST, uploader);
                tag.setField(FieldKey.TITLE, title);
                fileUtil.downloadImage("https://img.youtube.com/vi/"+info[2]+"/maxresdefault.jpg","img.jpg");
                Artwork cover = Artwork.createArtworkFromFile(new File("img.jpg"));
                tag.addField(cover);
                f.commit();
                fileUtil.deleteFile("img.jpg");
                fileUtil.moveFile(fileUtil.findMP3File("downloaded").getAbsolutePath(), "completed/" + fileUtil.removeNonAlphaNumeric(info[0]) + " ["+info[2]+ "].mp3");
                outputArea.setText(outputArea.getText()+"\n"+"Moved file to Completed Folder");
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
    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message, "JUST YOUR FRIENDLY NEIGHBORLY REMINDER", JOptionPane.WARNING_MESSAGE);
    }
    public static void youtubeToMP3(String url) {
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




}
