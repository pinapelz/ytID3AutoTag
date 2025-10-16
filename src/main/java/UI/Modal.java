package UI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class Modal {
    private static final String[] validBrowsers = {"brave", "chrome", "chromium", "edge", "firefox", "opera", "safari", "vivaldi", "whale"};
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
    public static String showDirectoryChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }

    public static String showDirectoryChooser(String startDir) {
        JFileChooser chooser = new JFileChooser(startDir);
        chooser.setDialogTitle("Select a directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
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

    /**
     * Show warning message
     */
    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message, "WARNING/REMINDER", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Show error message
     */
    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    public static String textAreaDialog(String text, String title) {
        JTextArea textArea = new JTextArea(text);
        textArea.setColumns(30);
        textArea.setRows(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        int ret = JOptionPane.showConfirmDialog(null, new JScrollPane(textArea), title, JOptionPane.OK_OPTION);
        if (ret == 0) {
            return textArea.getText();
        }
        return null;
    }

    public static String chooseBrowserType() {
        JComboBox<String> dropdown = new JComboBox<>(validBrowsers);
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Please select a browser you have used to sign into YouTube with on this device:"), BorderLayout.NORTH);
        panel.add(dropdown, BorderLayout.CENTER);
        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Select Browser",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            return (String) dropdown.getSelectedItem();
        } else {
            return "";
        }
    }



}
