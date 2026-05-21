import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class TagEditorScreen extends JFrame {
    private static final int COLUMN_TITLE = 0;
    private static final int COLUMN_ARTIST = 1;
    private static final int COLUMN_FILE_NAME = 2;
    private static final int COLUMN_FILE_PATH = 3;

    private JPanel mainPanel;
    private JTextField titleField;
    private JLabel titleLabel;
    private JTextField uploaderField;
    private JLabel uploaderLabel;
    private JTextField fileNameField;
    private JLabel fileNameLabel;
    private JTextField imagePathField;
    private JButton imageChooseButton;
    private JTable songTable;
    private JButton chooseAudioDirectoryButton;
    private JButton applyChangesButton;
    private JLabel artIconLabel;
    private JTextField searchField;
    private JButton listenButton;
    private String setDirPath = "";
    private String selectedAlbumArt = "";
    private ArrayList<File> songList = new ArrayList<File>();
    private String currPath = "";
    private Boolean imageSelected = false;

    public TagEditorScreen() {
        // Initialize all components
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        titleField = new JTextField();
        titleLabel = new JLabel("Title:");
        uploaderField = new JTextField();
        uploaderLabel = new JLabel("Uploader:");
        fileNameField = new JTextField();
        fileNameLabel = new JLabel("Filename:");
        imagePathField = new JTextField();
        imageChooseButton = new JButton("Choose Image");
        songTable = new JTable();
        chooseAudioDirectoryButton = new JButton("Choose Audio Directory");
        applyChangesButton = new JButton("Apply Changes");
        artIconLabel = new JLabel();
        searchField = new JTextField();
        listenButton = new JButton("Listen");
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setSize(700, 550);
        initalizeListeners();
        initializeTable();
        setupLayout();
        listenButton.setEnabled(false);
        this.add(mainPanel);
        this.setVisible(true);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                String searchTerm = searchField.getText();
                clearSongTable();
                for (File f : songList) {
                    if (f.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                        addSongTable(f);
                    }
                }

            }
        });
    }

    private void initializeTable() {
        if (songTable == null) {
            songTable = new JTable();
        }
        songTable.setModel(new DefaultTableModel(null, new String[]{"Title", "Artist", "Filename", "Filepath"}));
        // Keep full path available internally but not visible in the UI.
        songTable.removeColumn(songTable.getColumnModel().getColumn(COLUMN_FILE_PATH));
        songTable.getTableHeader().setReorderingAllowed(false);
    }

    private void clearSongTable() {
        DefaultTableModel dtm = (DefaultTableModel) songTable.getModel();
        dtm.setRowCount(0);
    }

    private void addSongTable(File audioFile) {
        try {
            AudioFile f = AudioFileIO.read(audioFile);
            Tag tag = f.getTag();
            DefaultTableModel model = (DefaultTableModel) songTable.getModel();
            model.addRow(new Object[]{
                    tag.getFirst(FieldKey.TITLE),
                    tag.getFirst(FieldKey.ARTIST),
                    audioFile.getName(),
                    audioFile.getAbsolutePath()
            });
        } catch (Exception e) {
            UI.Modal.showError("Error while adding song to table, wasn't able to read file: " + audioFile.getAbsolutePath());
        }
    }

    private String getPathForRow(int row) {
        return songTable.getModel().getValueAt(row, COLUMN_FILE_PATH).toString();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }

    private void populateFields(File audioFile) {
        try {
            AudioFile f = AudioFileIO.read(audioFile);
            Tag tag = f.getTag();
            titleField.setText(tag.getFirst(FieldKey.TITLE));
            uploaderField.setText(tag.getFirst(FieldKey.ARTIST));
            fileNameField.setText(audioFile.getName());
            Artwork albumArt = tag.getFirstArtwork();
            ImageIcon albumArtIcon = new ImageIcon(resizeImage(albumArt.getImage(), 320, 180));
            artIconLabel.setIcon(albumArtIcon);
            artIconLabel.setText("");
            listenButton.setEnabled(true);

        } catch (Exception e) {
            UI.Modal.showError("Error while populating fields, wasn't able to read file: " + audioFile.getAbsolutePath());
        }

    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    private void playMP3(String filepath) {
        try {
            File file = new File(filepath);
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateSongList(){
        songList = FileUtility.getMp3FilesAsList(setDirPath); //get arraylist of all files in the directory
        for (File file : songList) {
            addSongTable(file);
        }
    }


    private void setupLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title row
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(titleField, gbc);
        
        // Uploader row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(uploaderLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(uploaderField, gbc);
        
        // Filename row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(fileNameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(fileNameField, gbc);

        // Image path row
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Album Art:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(imagePathField, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(imageChooseButton, gbc);
        
        // Art icon
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        mainPanel.add(artIconLabel, gbc);
        
        // Search field
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridheight = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(searchField, gbc);
        
        // Song table
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(songTable);
        mainPanel.add(scrollPane, gbc);
        
        // Bottom buttons
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(chooseAudioDirectoryButton, gbc);
        
        gbc.gridx = 1;
        mainPanel.add(listenButton, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        mainPanel.add(applyChangesButton, gbc);
    }
    
    private void initalizeListeners() {
        chooseAudioDirectoryButton.addActionListener(e -> {
            clearSongTable();
            setDirPath = UI.Modal.showDirectoryChooser();
            populateSongList();
        });

        songTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                populateFields(new File(getPathForRow(songTable.getSelectedRow())));
                currPath = getPathForRow(songTable.getSelectedRow());
                imageSelected = false;
            }
        });

        songTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    try {
                        populateFields(new File(getPathForRow(songTable.getSelectedRow() + 1)));
                        currPath = getPathForRow(songTable.getSelectedRow() + 1);
                        imageSelected = false;
                    } catch (Exception ex) {
                        UI.Modal.showError("Seems that we aren't able to move down a row for some reason...");
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    try {
                        populateFields(new File(getPathForRow(songTable.getSelectedRow() - 1)));
                        currPath = getPathForRow(songTable.getSelectedRow() - 1);
                        imageSelected = false;
                    } catch (Exception ex) {
                        UI.Modal.showError("Seems that we aren't able to move up a row for some reason...");
                    }
                }
            }
        });
        applyChangesButton.addActionListener(e -> {
            try {
                int selectedRow = songTable.getSelectedRow();
                if (selectedRow == -1 || currPath.isEmpty()) {
                    UI.Modal.showError("Please select a song first.");
                    return;
                }

                DefaultTableModel model = (DefaultTableModel) songTable.getModel();
                String editedFileName = fileNameField.getText().trim();
                if (editedFileName.isEmpty()) {
                    editedFileName = model.getValueAt(selectedRow, COLUMN_FILE_NAME).toString().trim();
                }
                if (editedFileName.isEmpty() || editedFileName.contains("/") || editedFileName.contains("\\")) {
                    UI.Modal.showError("Invalid file name.");
                    return;
                }

                File currentFile = new File(currPath);
                String currentExtension = getFileExtension(currentFile.getName());
                if (!currentExtension.isEmpty() && !editedFileName.toLowerCase().endsWith(currentExtension.toLowerCase())) {
                    editedFileName += currentExtension;
                }

                File targetFile = new File(currentFile.getParentFile(), editedFileName);
                if (!currentFile.getName().equals(editedFileName)) {
                    if (targetFile.exists()) {
                        UI.Modal.showError("A file with that name already exists.");
                        return;
                    }
                    if (!currentFile.renameTo(targetFile)) {
                        UI.Modal.showError("Failed to rename file.");
                        return;
                    }
                    currPath = targetFile.getAbsolutePath();
                    model.setValueAt(targetFile.getName(), selectedRow, COLUMN_FILE_NAME);
                    fileNameField.setText(targetFile.getName());
                }

                AudioFile f = AudioFileIO.read(new File(currPath));
                Tag tag = f.getTag();
                tag.setField(FieldKey.TITLE, titleField.getText());
                tag.setField(FieldKey.ARTIST, uploaderField.getText());
                if (imageSelected) {
                    tag.deleteArtworkField();
                    Artwork cover = Artwork.createArtworkFromFile(new File(selectedAlbumArt));
                    tag.addField(cover);
                    System.out.println("Changed the Artwork");
                }
                f.commit();
                clearSongTable();
                songList = FileUtility.getMp3FilesAsList(setDirPath); //get arraylist of all files in the directory
                for (File file : songList) {
                    addSongTable(file);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
        imageChooseButton.addActionListener(e -> {
            try {
                selectedAlbumArt = UI.Modal.showImageFileChooser();
                if(selectedAlbumArt == null){
                    return;
                }
                File selectedFile = new File(selectedAlbumArt);
                BufferedImage selectedImage;
                selectedImage = ImageIO.read(selectedFile);
                ImageIcon albumArtIcon = new ImageIcon(resizeImage(selectedImage, 260, 180));
                artIconLabel.setText(selectedFile.getName());
                artIconLabel.setIcon(albumArtIcon);
                imageSelected = true;

            } catch (Exception ex) {
                UI.Modal.showError("Error while selecting image");
            }

        });
        listenButton.addActionListener(e -> {
            playMP3(currPath);


        });
    }


}
