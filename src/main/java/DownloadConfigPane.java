import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class DownloadConfigPane extends JFrame{
    private JPanel mainPanel;
    private JTable outputTable;
    private JTextField urlField;
    private JTextField fromField;
    private JTextField toField;
    private JButton loadFromFileButton;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JLabel urlLabel;
    private JButton addButton;
    private JButton saveButton;
    private JButton removeButton;
    private JScrollPane tableScrollPane;
    private JCheckBox fullVideoCheckBox;

    private String loadedPath;

    public DownloadConfigPane() {
        // Initialize all components
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        urlField = new JTextField();
        fromField = new JTextField("HH:MM:SS");
        toField = new JTextField("HH:MM:SS");
        loadFromFileButton = new JButton("Load From File");
        fromLabel = new JLabel("From:");
        toLabel = new JLabel("To:");
        urlLabel = new JLabel("URL");
        addButton = new JButton("Add");
        saveButton = new JButton("Save");
        removeButton = new JButton("Remove");
        fullVideoCheckBox = new JCheckBox("Full Video");
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setSize(900, 500);
        initializeTable();
        setupLayout();
        this.add(mainPanel);
        this.setVisible(true);
        loadFromFileButton.addActionListener(e -> loadConfigFromFile());
        addButton.addActionListener(e -> {
            String url = urlField.getText();
            if(url.contains("youtu.be")){
                String[] split = url.split("/");
                url = "https://www.youtube.com/watch?v=" + split[split.length - 1];
            }
            else if(url.contains("playlist") || url.contains("import")) {
                String playlistUrls =
                        UI.Modal.textAreaDialog("Looks like you're trying to add a playlist or import by plaintext urls\n" +
                                        "please use a tool https://cable.ayra.ch/ytdl/playlist.php to get the individual video URLs\n" +
                                        "Delete all the text here and paste them here!",
                                "YouTube Playlist Import");
                try {
                    assert playlistUrls != null;
                    String[] urls = playlistUrls.split("\n");
                    for (String playlistUrl : urls) {
                        addURLToTable(playlistUrl, "00:00:00", "00:00:00");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid playlist URLs. Make sure" +
                            "you only have 1 URL on each line in the text area and that they are valid YouTube video URLs");
                }
            }
            String from = fromField.getText();
            String to = toField.getText();
            addURLToTable(url, from, to);

        });
        removeButton.addActionListener(e -> {
            int row = outputTable.getSelectedRow();
            if (row == -1){
                return;
            }
            DefaultTableModel model = (DefaultTableModel) outputTable.getModel();
            model.removeRow(row);
        });

        saveButton.addActionListener(e -> {
            try {
                saveConfigToFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        fullVideoCheckBox.addActionListener(e -> {
            if (fullVideoCheckBox.isSelected()){
                fromField.setEnabled(false);
                fromField.setText("BEGINNING_OF_VIDEO");
                toField.setEnabled(false);
                toField.setText("END_OF_VIDEO");
            }
            else{
                fromField.setEnabled(true);
                toField.setEnabled(true);
            }
        });
    }

    private void setupLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // First row - URL
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(urlLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(urlField, gbc);
        
        // Second row - From/To fields
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(fromLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        mainPanel.add(fromField, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(fullVideoCheckBox, gbc);
        
        gbc.gridx = 3;
        mainPanel.add(toLabel, gbc);
        
        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        mainPanel.add(toField, gbc);
        
        // Third row - Add button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(addButton, gbc);
        
        // Fourth row - Remove button
        gbc.gridy = 3;
        mainPanel.add(removeButton, gbc);
        
        // Fifth row - Table
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        tableScrollPane = new JScrollPane(outputTable);
        mainPanel.add(tableScrollPane, gbc);
        
        // Sixth row - Load and Save buttons
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(loadFromFileButton, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 3;
        mainPanel.add(saveButton, gbc);
    }
    
    private void initializeTable() {
        DefaultTableModel model = new DefaultTableModel();
        // center align the text in the table
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        outputTable = new JTable();
        outputTable.setDefaultRenderer(String.class, centerRenderer);
        model.addColumn("URL");
        model.addColumn("From");
        model.addColumn("To");
        outputTable.setModel(model);
        outputTable.getTableHeader().setReorderingAllowed(false);
    }

    private void clearTable(){
        DefaultTableModel model = (DefaultTableModel) outputTable.getModel();
        model.setRowCount(0);
    }

    private File selectTextFileChooser(){
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            return selectedFile;
        }
        return null;
    }

    private File selectDirectoryFileChooser(){
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            return selectedFile;
        }
        return null;
    }

    private void loadConfigFromFile(){
        File file = selectTextFileChooser();
        if (file == null){
            return;
        }
        clearTable();
        initializeTable();
        try{
            for (String line : Files.readAllLines(file.toPath())) {
                String[] split = line.split(",");
                String url = split[0];
                String[] timeRange = new String[2];
                if (split.length == 2){
                    timeRange = split[1].split("-");
                }
                else{
                    timeRange[0] = "00:00:00";
                    timeRange[1] = "00:00:00";
                }
                String from = timeRange[0];
                String to = timeRange[1];
                if (from.length() != 8){
                    from = "00:00:00";
                }
                if (to.length() != 8){
                    to = "00:00:00";
                }
                Object[] song = new Object[]{url, from, to};
                DefaultTableModel model = (DefaultTableModel) outputTable.getModel();
                model.addRow(song);
                // add headers to the table
            }
            loadedPath = file.getAbsolutePath();
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null, "Please choose a file with the download config format");
            System.out.println("Invalid file selected");
            loadedPath = null;
        }

    }

    private void saveConfigToFile() throws IOException {
        if (loadedPath == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                loadedPath = fileToSave.getAbsolutePath();
                if (!loadedPath.endsWith(".txt")) {
                    loadedPath += ".txt";
                }
            } else {
                return;
            }
        }

        File saveFile = new File(loadedPath);
        FileWriter writer = new FileWriter(saveFile);
        for (int i = 0; i < outputTable.getRowCount(); i++) {
            String url = (String) outputTable.getValueAt(i, 0);
            String from = (String) outputTable.getValueAt(i, 1);
            String to = (String) outputTable.getValueAt(i, 2);
            String line = "";
            if (from.equals("00:00:00") && to.equals("00:00:00")) {
                line = url;
            } else {
                line = url + "," + from + "-" + to;
            }
            writer.write(line + System.lineSeparator());
            System.out.println(line);
        }
        writer.close();
        JOptionPane.showConfirmDialog(null, "Saved to " + loadedPath, "Saved", JOptionPane.DEFAULT_OPTION);
    }

    private void addURLToTable(String url, String from, String to){
        if (url.isEmpty()){
            return;
        }
        if (from.length() != 8){
            from = "00:00:00";
        }
        if (to.length() != 8){
            to = "00:00:00";
        }
        Object[] song = new Object[]{url, from, to};
        DefaultTableModel model = (DefaultTableModel) outputTable.getModel();
        model.addRow(song);
    }


}
