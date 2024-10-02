import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Configuration {
    /**
     * Reads the configuration json file and returns a hashmap with the kv pairs
     * @return HashMap of config data json
     */
    public HashMap<String, String> readConfigurationData() {
        HashMap<String, String> configData = new HashMap<>();
        File configFile = new File(System.getProperty("user.dir") +"/config/configuration.json");
        try (FileReader reader = new FileReader(configFile)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);

            for (String key : jsonObject.keySet()) {
                configData.put(key, jsonObject.getString(key));
            }
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Failed to read the configuration file.");
        }

        return configData;
    }

    /**
     * Creates a configuration file if one doesn't already exist
     * @return true/false if a file was created
     */
    public boolean createConfigurationFile(){
        if(new File(System.getProperty("user.dir") +"/config").mkdir())
            System.out.println("Created new config directory");
        File configFile = new File(System.getProperty("user.dir") +"/config/configuration.json");
        if(configFile.exists())
            return false; // File already exists
        System.out.println("Creating new Configuration File");
        JSONObject configurationData = new JSONObject();
        configurationData.put("lastFile","" );
        configurationData.put("outputPath", "completed");
        configurationData.put("blacklistFile", "");
        try (FileWriter file = new FileWriter(configFile)) {
            file.write(configurationData.toString(4));
            System.out.println("Successfully created a stub config file");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to create a stub config file");
            return false;
        }
    }

    public boolean modifyConfigurationValue(String key, String newValue) {
        File configFile = new File(System.getProperty("user.dir") +"/config/configuration.json");
        try (FileReader reader = new FileReader(configFile)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);

            if (!jsonObject.has(key)) {
                System.out.println("Key does not exist in the configuration.");
                return false;
            }

            jsonObject.put(key, newValue);

            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(jsonObject.toString(4)); // Indent with 4 spaces for readability
                return true;
            } catch (IOException e) {
                System.out.println("Failed to write to the configuration file.");
                return false;
            }
        } catch (IOException e) {
            System.out.println("Failed to read the configuration file.");
            return false;
        }
    }
}
