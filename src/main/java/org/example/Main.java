package org.example;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
       ArrayList<String> songs = readFile("songs.txt");
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
                moveFile(findMP3File("downloaded").getAbsolutePath(), "completed/" + removeNonAlphaNumeric(info[0]) + " ["+info[2]+ "].mp3");
            } catch (Exception e) {
                e.printStackTrace();

            }
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
//remove all non alphanumeric characters from string
    public static String removeNonAlphaNumeric(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }
    public static void moveFile(String source, String destination) {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);
        sourceFile.renameTo(destinationFile);
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
