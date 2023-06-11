package rpggamev2;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.event.MouseListener;

// custom JButton that sets the background image to iconName_hover.png when hovered
public class hoverButton extends JButton {
    String buttonName;

    hoverButton(String iconName, int x, int y, int width, int height) {
        super();
        this.setIcon(new ImageIcon(iconName + ".png"));
        this.setBounds(x, y, width, height);
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.addMouseListener(hovering);
        this.setVisible(false);
        buttonName = iconName;
    }

    MouseListener hovering = new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            setIcon(new ImageIcon(buttonName + "_hover.png"));
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            setIcon(new ImageIcon(buttonName + ".png"));
        }
    };
}
