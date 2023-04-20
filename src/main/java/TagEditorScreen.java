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
    private FileUtility fileUtil = new FileUtility();
    private String setDirPath = "";
    private String selectedAlbumArt = "";
    private ArrayList<File> songList = new ArrayList<File>();
    private String currPath = "";
    private Boolean imageSelected = false;

    public TagEditorScreen() {
        this.add(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setSize(700, 550);
        initializeTable();
        listenButton.setEnabled(false);
        initalizeListeners();
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
            e.printStackTrace();
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
        songList = fileUtil.getMp3Files(setDirPath); //get arraylist of all files in the directory
        for (int i = 0; i < songList.size(); i++) {
            addSongTable(songList.get(i));
        }
    }


    private void initalizeListeners() {
        chooseAudioDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearSongTable();
                setDirPath = fileUtil.showDirectoryChooser();
                populateSongList();
            }
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

                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    try {
                        populateFields(new File(songTable.getModel().getValueAt(songTable.getSelectedRow() - 1, 2).toString()));
                        currPath = songTable.getModel().getValueAt(songTable.getSelectedRow() - 1, 2).toString();
                        imageSelected = false;
                    } catch (Exception ex) {

                    }
                }
            }
        });
        applyChangesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                    songList = fileUtil.getMp3Files(setDirPath); //get arraylist of all files in the directory
                    for (int i = 0; i < songList.size(); i++) {
                        addSongTable(songList.get(i));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
        imageChooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    selectedAlbumArt = fileUtil.showImageFileChooser();
                    File selectedFile = new File(selectedAlbumArt);
                    BufferedImage selectedImage = null;
                    selectedImage = ImageIO.read(selectedFile);
                    ImageIcon albumArtIcon = new ImageIcon(resizeImage(selectedImage, 260, 180));
                    artIconLabel.setText(selectedFile.getName());
                    artIconLabel.setIcon(albumArtIcon);
                    imageSelected = true;

                } catch (Exception ex) {

                }

            }
        });
        listenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("CURRENT PATH " + currPath);
                playMP3(currPath);


            }
        });
    }


}
