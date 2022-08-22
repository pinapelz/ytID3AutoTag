import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TagEditorScreen extends JFrame{
    private JPanel mainPanel;
    private JTextField titleField;
    private JLabel titleLabel;
    private JTextField uploaderField;
    private JLabel uploaderLabel;
    private JTextField textField1;
    private JButton imageChooseButton;
    private JTable songTable;
    private JButton chooseAudioDirectoryButton;
    private JButton applyChangesButton;
    private JLabel artIconLabel;
    private JTextField textField2;
    private FileUtility fileUtil = new FileUtility();
    private String setDirPath = "";
    private ArrayList<File> songList = new ArrayList<File>();
    private long timeOfLastClick = 9999999;

    public TagEditorScreen(){
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setSize(600, 450);
        initializeTable();
        this.setVisible(true);
        chooseAudioDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearSongTable();
                setDirPath = fileUtil.showDirectoryChooser();
                songList = fileUtil.getMp3Files(setDirPath); //get arraylist of all files in the directory
                for(int i=0;i<songList.size();i++){
                   addSongTable(songList.get(i));

                }
            }
        });
        songTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
               populateFields(new File(songTable.getModel().getValueAt(songTable.getSelectedRow(),2 ).toString()));


            }
        });
        songTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode()==KeyEvent.VK_DOWN){
                    try {
                        populateFields(new File(songTable.getModel().getValueAt(songTable.getSelectedRow() + 1, 2).toString()));
                    }
                    catch(Exception ex){

                    }
                }
                else if(e.getKeyCode()==KeyEvent.VK_UP){
                    try {
                        populateFields(new File(songTable.getModel().getValueAt(songTable.getSelectedRow() - 1, 2).toString()));
                    }
                    catch(Exception ex){

                    }
                }
            }
        });
        applyChangesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }
    public void initializeTable(){
        songTable.setDefaultEditor(Object.class, null);
        songTable.setModel(new DefaultTableModel(null, new String[]{"Title", "Artist","Filepath"}));
        songTable.getTableHeader().setReorderingAllowed(false);
    }
    public void clearSongTable(){
        DefaultTableModel dtm = (DefaultTableModel) songTable.getModel();
        dtm.setRowCount(0);
    }
    public void addSongTable(File audioFile){
        try {
            AudioFile f = AudioFileIO.read(audioFile);
            Tag tag = f.getTag();
            DefaultTableModel model = (DefaultTableModel) songTable.getModel();
            model.addRow(new Object[]{tag.getFirst(FieldKey.TITLE), tag.getFirst(FieldKey.ARTIST),audioFile.getAbsolutePath()});
        }
        catch(Exception e){

        }
    }
//search for a file in a directory and return the file as a file
    public File searchForFile(String fileName, String directory){
        File[] files = new File(directory).listFiles();
        for(File file:files){
            if(file.getName().equals(fileName)){
                return file;
            }
        }
        return null;
    }

    public void populateFields(File audioFile){
        try {
            AudioFile f = AudioFileIO.read(audioFile);
            Tag tag = f.getTag();
            titleField.setText(tag.getFirst(FieldKey.TITLE));
            uploaderField.setText(tag.getFirst(FieldKey.ARTIST));
            Artwork albumArt = tag.getFirstArtwork();
            ImageIcon albumArtIcon = new ImageIcon(resizeImage(albumArt.getImage(),320,180));
            artIconLabel.setIcon(albumArtIcon);


        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }


}
