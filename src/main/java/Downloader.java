import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.*;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class Downloader {
    private final String outputDirectory;
    private final JTextArea outputArea;
    String[] formats = {"maxresdefault.jpg", "mqdefault.jpg", "hqdefault.jpg"};

    public Downloader(String outputDirectory, JTextArea outputArea){
        this.outputDirectory = outputDirectory;
        this.outputArea = outputArea;
    }

    /**
     * Tag mp3 with title, uploader, and image
     * @param uploader Uploader of the video
     * @param title Title of the video
     * @param imageUrl URL of to the thumbnail image
     */
    public boolean tagMp3InDir(String uploader, String title, String imageUrl, String filePath) {//Tag mp3 file in downloaded directory
        try {
            AudioFile f = AudioFileIO.read(FileUtility.findFileWithType(filePath, "mp3"));
            System.out.println("File found at: " + FileUtility.findFileWithType(filePath, "mp3"));
            Tag tag = f.getTag();
            System.out.println("Uploader: " + uploader);
            System.out.println("Title: " + title);
            tag.setField(FieldKey.ARTIST, uploader);
            tag.setField(FieldKey.TITLE, title);
            String pathToThumnail = FileUtility.downloadImage(imageUrl, "img.jpg", formats);
            Artwork cover = Artwork.createArtworkFromFile(new File(pathToThumnail));
            tag.addField(cover);
            f.commit();
            FileUtility.deleteFile(pathToThumnail);
    }
        catch(Exception e){
            JOptionPane.showMessageDialog(
                    null,
                    "Error occured while tagging mp3. Check your program version",
                    "ERROR", JOptionPane.ERROR_MESSAGE);
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
    public boolean download(String url, String stamp, String browser){
        ArrayList<String> times = new ArrayList<>(Arrays.asList(stamp.split("-")));
        String startTime = times.get(0);
        String endTime = times.get(1);
        int startSec = timestampToSeconds(startTime);
        int endSec = timestampToSeconds(endTime);
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "yt-dlp",
                    "--cookies-from-browser", browser,
                    "-4",
                    "--min-sleep-interval", "2",
                    "--max-sleep-interval", "7",
                    "--force-keyframes",
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
        }
        File downloadedWebm = FileUtility.findFileWithType(System.getProperty("user.dir"), "webm");
        if(downloadedWebm == null){
            return false;
        }

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

        String[] info = FileUtility.parseInfoJSON(FileUtility.jsonToString(FileUtility.findJsonFile(System.getProperty("user.dir"))));
        String uploader = info[1];
        String title = info[0];
        String urlID = info[2];
        String imageUrl = "https://img.youtube.com/vi/" + urlID+"/";
        File downloadedMp3 = FileUtility.findFileWithType(System.getProperty("user.dir"), "mp3");
        if(downloadedMp3 == null){
            return false;
        }
        String savedNonAlphaNumName;
        try{
             savedNonAlphaNumName = downloadedMp3.getName();
        }
        catch(NullPointerException ex){
            return false;
        }
        String tempRemoveAlphaNumeric = savedNonAlphaNumName.replaceAll("[^a-zA-Z0-9]", "") + ".mp3";
        if(!downloadedMp3.renameTo(new File(tempRemoveAlphaNumeric)))
            UI.Modal.showError("Error renaming file");
        tagMp3InDir(uploader, title, imageUrl, System.getProperty("user.dir"));
        FileUtility.deleteFile(downloadedWebm.getAbsolutePath());
        FileUtility.deleteFile(FileUtility.findJsonFile(System.getProperty("user.dir")));
        downloadedMp3 = FileUtility.findFileWithType(System.getProperty("user.dir"), "mp3");
        if(downloadedMp3 == null){
            return false;
        }
        if(!downloadedMp3.renameTo(new File(outputDirectory+"/"+savedNonAlphaNumName+"["+startTime+"-"+endTime+"].mp3"))){
            UI.Modal.showError("Error moving file to output directory");
            return false;
        }
        return true;
    }

    public boolean download(String url, String browser) {
        String ytDlpExecutable = "yt-dlp" + (System.getProperty("os.name").startsWith("Windows") ? ".exe" : "");
        try {
            String[] command = {
                    ytDlpExecutable,
                    "-4",
                    "--min-sleep-interval", "2",
                    "--max-sleep-interval", "7",
                    "--cookies-from-browser", browser,
                    "--extract-audio",
                    "--audio-format", "mp3", // force mp3 conversion
                    "--audio-quality", "0",
                    "--write-info-json",
                    "-o", "%(title)s[%(id)s].%(ext)s",
                    url
            };
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            Process process = processBuilder.start();
            relayConsole(process);
            process.waitFor();
        } catch (Exception e) {
            UI.Modal.showError("Ensure yt-dlp is installed. An error occurred while downloading using yt-dlp: " + e.getMessage());
            return false;
        }

        // Parse metadata
        String[] info = FileUtility.parseInfoJSON(FileUtility.jsonToString(FileUtility.findJsonFile(System.getProperty("user.dir"))));
        String uploader = info[1];
        String title = info[0];
        String urlID = info[2];
        String imageUrl = "https://img.youtube.com/vi/" + urlID + "/";

        File downloadedFile = FileUtility.findFileWithType(System.getProperty("user.dir"), "mp3");
        if (downloadedFile == null) {
            downloadedFile = FileUtility.findFileWithType(System.getProperty("user.dir"), "m4a");
            if (downloadedFile != null) {
                try {
                    String mp3Path = downloadedFile.getAbsolutePath().replace(".m4a", ".mp3");
                    String[] ffmpegCmd = {
                            "ffmpeg",
                            "-y", // overwrite output if exists
                            "-i", downloadedFile.getAbsolutePath(),
                            "-codec:a", "libmp3lame",
                            "-qscale:a", "0",
                            mp3Path
                    };
                    ProcessBuilder ffmpegBuilder = new ProcessBuilder(ffmpegCmd);
                    ffmpegBuilder.redirectErrorStream(true);
                    Process ffmpeg = ffmpegBuilder.start();
                    relayConsole(ffmpeg);
                    ffmpeg.waitFor();
                    downloadedFile.delete();
                    downloadedFile = new File(mp3Path);
                } catch (Exception e) {
                    UI.Modal.showError("Error converting m4a to mp3: " + e.getMessage());
                    return false;
                }
            }
        }
        if (downloadedFile == null) {
            UI.Modal.showError("No audio file was found after download.");
            return false;
        }
        String savedNonAlphaNumName = downloadedFile.getName();
        String tempRemoveAlphaNumeric = savedNonAlphaNumName.replaceAll("[^a-zA-Z0-9]", "") + ".mp3";
        File renamed = new File(tempRemoveAlphaNumeric);
        if (!downloadedFile.renameTo(renamed)) {
            UI.Modal.showError("Error renaming file");
            return false;
        }
        System.out.println("File renamed to: " + tempRemoveAlphaNumeric);
        FileUtility.deleteFile(FileUtility.findJsonFile(System.getProperty("user.dir")));
        tagMp3InDir(uploader, title, imageUrl, System.getProperty("user.dir"));
        File finalMp3 = FileUtility.findFileWithType(System.getProperty("user.dir"), "mp3");
        if (finalMp3 == null) {
            return false;
        }
        if (!finalMp3.renameTo(new File(outputDirectory + "/" + savedNonAlphaNumName))) {
            UI.Modal.showError("Error moving file to output directory");
            return false;
        }

        return true;
    }

    public static int timestampToSeconds(String timestamp) {
        int totalSeconds = 0;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime time = LocalTime.parse(timestamp, formatter);
            int hours = time.getHour();
            int minutes = time.getMinute();
            int seconds = time.getSecond();
            totalSeconds = hours * 3600 + minutes * 60 + seconds;
            System.out.println(totalSeconds);
        } catch (Exception e) {
            System.out.println("Error converting timestamp to seconds");
        }
        return totalSeconds;
    }

    public boolean videoIdAlreadyDownloaded(String videoId){
        return FileUtility.findFileContainingString(this.outputDirectory, videoId);
    }
}
