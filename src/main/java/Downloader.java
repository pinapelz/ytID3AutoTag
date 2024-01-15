import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class Downloader {
    private String outputDirectory;
    private JTextArea outputArea;
    private boolean removeNonAlphaNumeric;
    String formats[] = {"maxresdefault.jpg", "mqdefault.jpg", "hqdefault.jpg"};
    FileUtility fileUtil = new FileUtility();
    public Downloader(String outputDirectory, JTextArea outputArea, boolean removeNonAlphaNumeric){
        this.outputDirectory = outputDirectory;
        this.outputArea = outputArea;
        this.removeNonAlphaNumeric = removeNonAlphaNumeric;
    }

    public Downloader(String outputDirectory, JTextArea outputArea){
        this.outputDirectory = outputDirectory;
        this.outputArea = outputArea;
        this.removeNonAlphaNumeric = false;
    }

    /**
     * Tag mp3 with title, uploader, and image
     * @param uploader Uploader of the video
     * @param title Title of the video
     * @param imageUrl URL of to the thumbnail image
     */
    public boolean tagMp3InDir(String uploader, String title, String imageUrl, String filePath) {//Tag mp3 file in downloaded directory
        try {
            AudioFile f = AudioFileIO.read(fileUtil.findFileWithType(filePath, "mp3"));
            System.out.println("File found at: " + fileUtil.findFileWithType(filePath, "mp3"));
            Tag tag = f.getTag();
            System.out.println("Uploader: " + uploader);
            System.out.println("Title: " + title);
            tag.setField(FieldKey.ARTIST, uploader);
            tag.setField(FieldKey.TITLE, title);
            String pathToThumnail = fileUtil.downloadImage(imageUrl, "img.jpg", formats);
            Artwork cover = Artwork.createArtworkFromFile(new File(pathToThumnail));
            tag.addField(cover);
            f.commit();
            fileUtil.deleteFile(pathToThumnail);
    }
        catch(Exception e){
            JOptionPane.showMessageDialog(
                    null,
                    "Error occured while tagging mp3. Check your program version",
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Relays the console output from the CMD to the outputArea
     * @param p The process to relay to the outputArea from
     */
    public void relayConsole(Process p) {
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String cmd_line;
        while (true) {
            try {
                cmd_line = r.readLine();
                if (cmd_line == null) {
                    break;
                }
                System.out.println(cmd_line);
                outputArea.setText(outputArea.getText() + "\n" + cmd_line);
            }
            catch (IOException e) {
                System.out.println("Error while relaying from CMD");
            }
        }
    }


    /*
    Download a part of a video
     */
    public boolean download(String url, String stamp){
        ArrayList<String> times = new ArrayList<>(Arrays.asList(stamp.split("-")));
        String startTime = times.get(0);
        String endTime = times.get(1);
        int startSec = timestampToSeconds(startTime);
        int endSec = timestampToSeconds(endTime);
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "yt-dlp",
                    "-vU","--force-keyframes",
                    "-f", "bestaudio[ext=webm]",
                    "--download-sections","*"+startSec+"-"+endSec,
                     "-o", "%(title)s[%(id)s].%(ext)s",
                     "--write-info-json",
                    url
            );
            builder.directory(new File(System.getProperty("user.dir")));
            builder.redirectErrorStream(true);
            Process p = builder.start();
            relayConsole(p);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred while downloading using yt-dlp: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        File downloadedWebm = fileUtil.findFileWithType(System.getProperty("user.dir"), "webm");

        try{
            ProcessBuilder builder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", downloadedWebm.getAbsolutePath(),
                    "-vn",
                    "-ab", "128k",
                    "-ar", "44100",
                    "-y",
                    downloadedWebm.getAbsolutePath().replace(".webm", ".mp3")
            );
            builder.directory(new File(System.getProperty("user.dir")));
            builder.redirectErrorStream(true);
            Process p = builder.start();
            relayConsole(p);

        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "An error occurred while converting the webm file to mp3: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        String info[] = fileUtil.parseInfoJSON(fileUtil.jsonToString(fileUtil.findJsonFile(System.getProperty("user.dir"))));
        String uploader = info[1];
        String title = info[0];
        String urlID = info[2];
        String imageUrl = "https://img.youtube.com/vi/" + urlID+"/";
        File downloadedMp3 = fileUtil.findFileWithType(System.getProperty("user.dir"), "mp3");
        String savedNonAlphaNumName = downloadedMp3.getName();
        String tempRemoveAlphaNumeric = savedNonAlphaNumName.replaceAll("[^a-zA-Z0-9]", "") + ".mp3";
        downloadedMp3.renameTo(new File(tempRemoveAlphaNumeric));
        tagMp3InDir(uploader, title, imageUrl, System.getProperty("user.dir"));
        fileUtil.deleteFile(downloadedWebm.getAbsolutePath());
        fileUtil.deleteFile(fileUtil.findJsonFile(System.getProperty("user.dir")));
        downloadedMp3 = fileUtil.findFileWithType(System.getProperty("user.dir"), "mp3");
        downloadedMp3.renameTo(new File(outputDirectory+"/"+savedNonAlphaNumName));
        return true;
    }

    public boolean download(String url){
        String ytDlpExecutable = "yt-dlp" + (System.getProperty("os.name").startsWith("Windows") ? ".exe" : "");
        try {
            String[] command = {ytDlpExecutable, "-f", "bestaudio[ext=webm]", "-x", "--audio-format", "mp3", "--write-info-json", url, "-o", "%(title)s[%(id)s].%(ext)s"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            Process process = processBuilder.start();
            relayConsole(process);
            process.waitFor();
        } catch(Exception e){
            JOptionPane.showMessageDialog(
                    null,
                    "Error occured while downloading mp3. Check that you have yt-dlp installed",
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String info[] = fileUtil.parseInfoJSON(fileUtil.jsonToString(fileUtil.findJsonFile(System.getProperty("user.dir"))));
        String uploader = info[1];
        String title = info[0];
        String urlID = info[2];
        String imageUrl = "https://img.youtube.com/vi/" + urlID + "/";
        File downloadedMp3 = fileUtil.findFileWithType(System.getProperty("user.dir"), "mp3");
        String savedNonAlphaNumName = downloadedMp3.getName();
        String tempRemoveAlphaNumeric = savedNonAlphaNumName.replaceAll("[^a-zA-Z0-9]", "") + ".mp3";
        downloadedMp3.renameTo(new File(tempRemoveAlphaNumeric));
        System.out.println("File renamed to: " + tempRemoveAlphaNumeric);
        fileUtil.deleteFile(fileUtil.findJsonFile(System.getProperty("user.dir")));
        tagMp3InDir(uploader, title, imageUrl, System.getProperty("user.dir"));
        downloadedMp3 = fileUtil.findFileWithType(System.getProperty("user.dir"), "mp3");
        downloadedMp3.renameTo(new File(outputDirectory+"/"+savedNonAlphaNumName));
        return true;
    }


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
