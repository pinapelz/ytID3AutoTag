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
    private JPanel mainPanel;
    private JTextField titleField;
    private JLabel titleLabel;
    private JTextField uploaderField;
    private JLabel uploaderLabel;
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
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setSize(700, 550);
        initalizeListeners();
        initializeTable();
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
        songTable.setDefaultEditor(Object.class, null);
        songTable.setModel(new DefaultTableModel(null, new String[]{"Title", "Artist", "Filepath"}));
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
            model.addRow(new Object[]{tag.getFirst(FieldKey.TITLE), tag.getFirst(FieldKey.ARTIST), audioFile.getAbsolutePath()});
        } catch (Exception e) {
            UI.Modal.showError("Error while adding song to table, wasn't able to read file: " + audioFile.getAbsolutePath());
        }
    }

    private void populateFields(File audioFile) {
        try {
            AudioFile f = AudioFileIO.read(audioFile);
            Tag tag = f.getTag();
            titleField.setText(tag.getFirst(FieldKey.TITLE));
            uploaderField.setText(tag.getFirst(FieldKey.ARTIST));
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
                populateFields(new File(songTable.getModel().getValueAt(songTable.getSelectedRow(), 2).toString()));
                currPath = songTable.getModel().getValueAt(songTable.getSelectedRow(), 2).toString();
                imageSelected = false;
            }
        });

        songTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    try {
                        populateFields(new File(songTable.getModel().getValueAt(songTable.getSelectedRow() + 1, 2).toString()));
                        currPath = songTable.getModel().getValueAt(songTable.getSelectedRow() + 1, 2).toString();
                        imageSelected = false;
                    } catch (Exception ex) {
                        UI.Modal.showError("Seems that we aren't able to move down a row for some reason...");
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    try {
                        populateFields(new File(songTable.getModel().getValueAt(songTable.getSelectedRow() - 1, 2).toString()));
                        currPath = songTable.getModel().getValueAt(songTable.getSelectedRow() - 1, 2).toString();
                        imageSelected = false;
                    } catch (Exception ex) {
                        UI.Modal.showError("Seems that we aren't able to move up a row for some reason...");
                    }
                }
            }
        });
        applyChangesButton.addActionListener(e -> {
            try {
                System.out.println("CURRENT PATH " + currPath);
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
            System.out.println("CURRENT PATH " + currPath);
            playMP3(currPath);


        });
    }


}
