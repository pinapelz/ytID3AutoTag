import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtility {
    public void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public void downloadImage(String url, String fileName,String[] formats)  {
        boolean successfulDownload = false;
        int formatIndex = 0;
        while(!successfulDownload) {
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                URL urlObj = new URL(url+formats[formatIndex]);
                InputStream is = urlObj.openStream();
                byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) != -1) {
                    fos.write(b, 0, length);
                }
                fos.close();
                is.close();
                successfulDownload = true;
            } catch (Exception e) {
                formatIndex++;
            }
        }

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
    public static ArrayList<String> txtToArrayList(String fileName) {
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
    public ArrayList<File> getMp3Files(String path){
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
