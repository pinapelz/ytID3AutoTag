package UI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Modal {
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

    /**
     * Show warning message
     */
    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message, "JUST YOUR FRIENDLY NEIGHBORLY REMINDER", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Show error message
     */
    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }
}
