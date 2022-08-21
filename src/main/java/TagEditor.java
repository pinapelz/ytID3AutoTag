import javax.swing.*;
import java.awt.*;

public class TagEditor extends JFrame {
    JPanel mainPanel = new JPanel();
    GridBagConstraints constraints = new GridBagConstraints();
    public TagEditor(){
        mainPanel.setLayout(new GridBagLayout());
       this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
