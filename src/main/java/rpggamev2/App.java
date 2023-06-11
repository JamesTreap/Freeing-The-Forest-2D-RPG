package rpggamev2;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class App extends JFrame implements KeyListener {
    static gameBoard g;
    static Container pane;
    static JPanel titlePanel;
    static JButton startButton;
    static JButton instructionButton;
    static JButton returnButton;
    static JButton startInstButton;
    static Font sidebarFont = new Font("TimesRoman", Font.BOLD, 30);

    // initialization -------------------------------------------------------------------
    App() {
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        startButton = new hoverButton("./images/buttons/startbutton", 263, 234, 514, 142);
        instructionButton = new hoverButton("./images/buttons/instructionbutton", 264, 350, 514, 142);
        startInstButton = new hoverButton("./images/buttons/instplay", 701, 467, 299, 82);
        returnButton = new hoverButton("./images/buttons/instback", 35, 467, 299, 82);
    }
    
    // key pressed methods ------------------------------------------------------------------------
    // determines what direction to move the player on the board screen
    public void keyPressed(KeyEvent e) {
        // don't allow user to move if not in the gameboard state    
        if (g.getState() != menuState.PLAY) {
            return;
        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            g.movePlayerDown();
        } else if (e.getKeyCode() == KeyEvent.VK_W) {
            g.movePlayerUp();
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            g.movePlayerLeft();
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            g.movePlayerRight();
        }
        repaint();
    }
    
    // required methods for KeyListener() - implementation purposefully blank
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // event listener methods for buttons ---------------------------------------------------------
    // starts the game
    static ActionListener starting = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Starting game!");
            startButton.setVisible(false);
            instructionButton.setVisible(false);
            returnButton.setVisible(false);
            startInstButton.setVisible(false);
            g.startGame();
        }
    };

    // sends the user to the instruction screen
    static ActionListener instruct = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // remove visibility of all buttons
            startButton.setVisible(false);
            instructionButton.setVisible(false);
            returnButton.setVisible(true);
            startInstButton.setVisible(true);
            g.instructUser();
        }
    };

    // sends the user to the main menu
    static ActionListener wantMenu = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // remove visibility of all buttons
            startButton.setVisible(true);
            instructionButton.setVisible(true);
            returnButton.setVisible(false);
            startInstButton.setVisible(false);
            g.menuButton.setVisible(false);
            g.goMenu();
        }
    };

    // main method --------------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("Welcome to the Treap RPG Game!");
        System.out.println("Not as extravagant as Undertale, but equally as fun.");

        // initialize app
        App f = new App();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setTitle("Treap RPG Game");
        f.setSize(1054,638);
        f.setResizable(false);
        
        // initialize the board
        pane = f.getContentPane();
        g = new gameBoard();
        g.setBounds(0, 0, 1040, 600);
        g.setLayout(null);
        pane.add(g);

        // add actionlisteners to the buttons for the various menus
        startButton.addActionListener(starting);
        startButton.setVisible(true);
        
        instructionButton.addActionListener(instruct);
        instructionButton.setVisible(true);

        startInstButton.addActionListener(starting);
        returnButton.addActionListener(wantMenu);

        g.add(startButton);
        g.add(instructionButton);
        g.add(returnButton);
        g.add(startInstButton);
        f.setVisible(true);

        // ----------------------------------------------------------------------------------------
        // core game loop
        System.out.println( "End of main.");
    }
}
