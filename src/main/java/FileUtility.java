import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileUtility {

    public void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public void deleteALlFileOfType(String path, String fileExt){
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for(int i=0;i<listOfFiles.length;i++){
            if(listOfFiles[i].isFile()){
                if(listOfFiles[i].getName().endsWith(fileExt)){
                    listOfFiles[i].delete();
                }
            }
        }
    }

    public String downloadImage(String url, String fileName,String[] formats)  {
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

    public String removeBlacklist(String s, String filename){
        HashMap<String, String> blacklist = arrayListToHashMap(readTextFile(filename),":");
        for(String key : blacklist.keySet()){
            if(s.contains(key)){
                s = s.replace(key,blacklist.get(key));
            }
        }
        return s;


    }
    //read a text file and return the contents as a hashmap with key value pairs
    public ArrayList<String> readTextFile(String fileName) {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
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

    public HashMap<String, String> arrayListToHashMap(ArrayList<String> list, String delimiter) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (String line : list) {
            String[] parts = line.split(delimiter);
            if (parts.length >= 2) {
                String key = parts[0];
                String value = parts[1];
                map.put(key, value);
            }
            else if(parts.length==1){
                String key = parts[0];
                String value  = "";
                map.put(key, value);
            }
            else {
                System.out.println("ignoring line: " + line);
            }
        }
        return map;
    }

    public void moveFile(String source, String destination) {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);
        sourceFile.renameTo(destinationFile);
        System.out.println("Moved file to Completed Folder");
    }

    public String removeNonAlphaNumeric(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static File findFileWithType(String directory, String fileExt){
        System.out.println("Searching for file with extension: " + fileExt + " in directory: " + directory);
        File dir = new File(directory);
        File[] files = dir.listFiles();
        for(File file : files){
            if(file.getName().endsWith(fileExt)){
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
    public static void deleteAllFilesDir(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    public static String showImageFileChooser() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPEG Image File", "jpg", "jpeg");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Select a image file");
        chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }



    public String showDirectoryChooser(){
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile().getAbsolutePath();
            }
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null,"An unexpected error has occured");
        }
        return "";
    }
    //get the path of all mp3 files in a directory and return them as a file arraylist
    public ArrayList<File> getMp3FilesAsList(String path){
        ArrayList<File> mp3Files = new ArrayList<File>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for(int i=0;i<listOfFiles.length;i++){
            if(listOfFiles[i].isFile()){
                if(listOfFiles[i].getName().endsWith(".mp3")){
                    mp3Files.add(listOfFiles[i]);
                }
            }
        }
        return mp3Files;
    }
}
