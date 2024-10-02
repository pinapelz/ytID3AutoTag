import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;


public class FileUtility {

    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            if(!file.delete())
                System.out.println("Failed to delete file: " + fileName);
        }
    }

    public static void deleteALlFileOfType(String path, String fileExt){
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for(int i = 0; i< Objects.requireNonNull(listOfFiles).length; i++){
            if(listOfFiles[i].isFile()){
                if(listOfFiles[i].getName().endsWith(fileExt)){
                    listOfFiles[i].delete();
                }
            }
        }
    }

    public static String downloadImage(String url, String fileName, String[] formats)  {
        boolean successfulDownload = false;
        int formatIndex = 0;
        String pathToImage = "";
        while(!successfulDownload) {
            System.out.println("Attempting to download image at: " + url+formats[formatIndex]);
            // attempt to download image
            for (int i = 0; i < 3; i++) {
                try {
                    URL imageUrl = new URL(url + formats[formatIndex]);
                    InputStream in = imageUrl.openStream();
                    Path path = Paths.get(fileName);
                    OutputStream out = new BufferedOutputStream(Files.newOutputStream(path));
                    for (int b; (b = in.read()) != -1; ) {
                        out.write(b);
                    }
                    out.close();
                    in.close();
                    successfulDownload = true;
                    pathToImage = path.toAbsolutePath().toString();
                    break;
                } catch (Exception e) {
                    System.out.println("Failed to download image at: " + url+formats[formatIndex]);
                    System.out.println("Retrying...");
                }
            }

        }
        System.out.println("Image downloaded");
        return pathToImage;
    }

    public static File findFileWithType(String directory, String fileExt){
        System.out.println("Searching for file with extension: " + fileExt + " in directory: " + directory);
        File dir = new File(directory);
        File[] files = dir.listFiles();
        assert files != null;
        for(File file : files){
            if(file.getName().endsWith(fileExt)){
                System.out.println("Found file: " + file.getName());
                return file;
            }
        }
        System.out.println("No file found with extension: " + fileExt);
        return null;
    }

    public static String findJsonFile(String folderName) {
        File folder = new File(folderName);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().endsWith(".json")) {
                    return listOfFiles[i].getAbsolutePath();
                }
            }
        }
        return null;
    }
    public static String[] parseInfoJSON(String json) {
        JSONObject obj = new JSONObject(json);
        String title = obj.getString("fulltitle");
        String uploader = obj.getString("uploader");
        String id = obj.getString("id");
        String[] info = {title,uploader,id};
        return info;

    }
    public static String jsonToString(String fileName) {
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
        } catch (Exception ignored) {
        }
        return json;
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
            System.out.println("Error reading file: " + fileName);
        }
        return lines;
    }

    //get the path of all mp3 files in a directory and return them as a file arraylist
    public static ArrayList<File> getMp3FilesAsList(String path){
        ArrayList<File> mp3Files = new ArrayList<File>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for(int i = 0; i< Objects.requireNonNull(listOfFiles).length; i++){
            if(listOfFiles[i].isFile()){
                if(listOfFiles[i].getName().endsWith(".mp3")){
                    mp3Files.add(listOfFiles[i]);
                }
            }
        }
        return mp3Files;
    }

}
