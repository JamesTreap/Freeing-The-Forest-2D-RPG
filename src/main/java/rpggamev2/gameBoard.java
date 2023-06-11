package rpggamev2;

import java.io.*;                       // for scanner
import java.util.Scanner;               // for reading in the map from a .csv file
import javax.swing.*;                   // for JPanel class
import java.awt.*;                      // for graphics class
import java.awt.event.ActionEvent;      // for updating the screen at a dedicated framerame
import java.awt.event.ActionListener;   // for updating the screen at a dedicated framerame
import java.lang.Math;

// Import a bunch-o-libraries for playing audio!
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class gameBoard extends JPanel implements ActionListener {
    //  constants for initiatlizing the board
    private final int BOARDWIDTH = 20;
    private final int BOARDHEIGHT = 20;
    private final int TILEPIXELSIZE = 30;
    private final int FLOWERHEAL = 5;
    private final int SWORDATK = 5;
    private final int SHIELDDEF = 5;
    private final Font sidebarFont = new Font("TimesRoman", Font.BOLD, 30);
    private final int startingHP = 20;
    private final int startingATK = 5;
    private final int startingDEF = 5;

    // constants for player direction
    private final char UP = 'n';
    private final char DOWN = 's';
    private final char LEFT = 'w';
    private final char RIGHT = 'e';

    // constants for displaying animations
    private final int TILEFRAMES = 4;               // number of frames for each tile
    private final int SPLASHFRAMES = 15;            // number of frames for splash animation
    private final int DEATHFRAMES = 15;             // number of frames for death animation
    private final int FRAMESPERSECOND = 50;         // FPS = 1000 / FRAMESPERSECOND, e.g 50ms = 20fps

    // mutatable variables
    private int splashcounter = 0;                  // for the splash animation
    private int deathcounter = 0;                   // for the death animation
    private int frameNumber = 0;                    // loops from 0-3
    private int frameHelper = 0;                    // animation is too fast, use framehelper to slow it down
    private int numEnemies;                         // represents the number of enemies

    private char[][] boardTiles = new char[BOARDWIDTH][BOARDHEIGHT];  // access tiles via boardTiles[X-coord][Y-coord] 
    private String mapFile = "./map1.csv";                            // represents the map file
    private String overworldTheme = "./music/overworld_theme.wav";    // music theme
    private String enemyLevel = "?";                                  // make this a string so I can display '??' during bossfight

    private Timer timer;                            // for updating the console 20 times a second
    private player thePlayer;                       // from the player class
    private player theEnemy;                        // represents the enemy    
    private menuState gamestate = menuState.TITLE;  // can access state via .getState() method   
    private battleState playerState  = battleState.CHOOSE;
    private battleState enemyState;
    private itemState currItem = itemState.NONE;

    // static images
    private Image titlescreen;
    private Image instructscreen;
    private Image victoryscreen;
    private Image grass;
    private Image sidebar;
    private Image tree;
    private Image battleArena;
    private Image battleBar;

    // images with multiple frames
    private Image[] playerIconUp = new Image[TILEFRAMES];
    private Image[] playerIconDown = new Image[TILEFRAMES];
    private Image[] playerIconRight = new Image[TILEFRAMES];
    private Image[] playerIconLeft = new Image[TILEFRAMES];
    private Image[] playerWaterUp = new Image[TILEFRAMES];
    private Image[] playerWaterDown = new Image[TILEFRAMES];
    private Image[] playerWaterRight = new Image[TILEFRAMES];
    private Image[] playerWaterLeft = new Image[TILEFRAMES];

    private Image[] water = new Image[TILEFRAMES];
    private Image[] flower = new Image[TILEFRAMES];
    private Image[] chest = new Image[TILEFRAMES];
    private Image[] stone = new Image[TILEFRAMES];
    private Image[] enemy = new Image[TILEFRAMES];
    private Image[] boss = new Image[TILEFRAMES];
    private Image[] splashes = new Image[SPLASHFRAMES];
    private Image[] deathies = new Image[DEATHFRAMES];

    JButton menuButton = new hoverButton("./images/buttons/mainmenu", 263, 234, 514, 250);
    JButton atkButton = new hoverButton("./images/buttons/attack", 8, 483, 184, 84);
    JButton buffButton = new hoverButton("./images/buttons/buff", 208, 483, 184, 84);
    JButton defButton = new hoverButton("./images/buttons/block", 408, 483, 184, 84);

    // initialization of the board
    gameBoard() {
        super();
        this.setFont(sidebarFont);
        this.add(menuButton);
        this.add(atkButton);
        this.add(defButton);
        this.add(buffButton);

        loadImage();
        menuButton.addActionListener(App.wantMenu);
        atkButton.addActionListener(doAnAttack);
        defButton.addActionListener(doABlock);
        buffButton.addActionListener(doABuff);
        timer = new Timer(FRAMESPERSECOND, this);
        timer.start();

        try {
            File audioFile = new File(overworldTheme);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();   
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.open(audioStream);
            audioClip.start();
        } catch (UnsupportedAudioFileException ex) {
            System.out.println("The specified audio file is not supported.");
            ex.printStackTrace();
        } catch (LineUnavailableException ex) {
            System.out.println("Audio line for playing back is unavailable.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error playing the audio file.");
            ex.printStackTrace();
        }
    }

    // updates the game at a dedicated FPS rate - 20fps for screens, 5fps for board and combat
    public void actionPerformed(ActionEvent e) {
        frameHelper++;
        frameHelper = frameHelper % TILEFRAMES;
        if (frameHelper == 0) {
            frameNumber++;
            frameNumber = frameNumber % TILEFRAMES;
        }
        repaint();
    }

    // methods for displaying the game ------------------------------------------------------------
    // displays the board
    public void paintComponent (Graphics g) {
        super.paintComponent(g);

        if (gamestate == menuState.TITLE) {
            g.drawImage(titlescreen, 0, 0, this);
            return;
        }

        if (gamestate == menuState.INSTRUCT) {
            g.drawImage(instructscreen, 0, 0, this);
            return;
        }

        if (gamestate == menuState.VICTORY) {
            g.drawImage(victoryscreen, 0, 0, this);
            return;
        }

        checkDrowned();
        if (gamestate == menuState.DROWNED || gamestate == menuState.DROWNED_DONE) {
            drawDrowned(g);
            return;
        }

        checkDied();
        if (gamestate == menuState.DEAD || gamestate == menuState.DEAD_DONE) {
            drawDied(g);
            return;
        }
        
        // draw side panel and stats
        g.drawImage(sidebar, TILEPIXELSIZE * BOARDWIDTH, 0, this);

        // if the user consumed an item
        pickedItem();
        if (currItem == itemState.FLOWER) {
            drawTextAction(g, "You picked up a", "flower. +" + FLOWERHEAL + "HP!", "");
        } else if (currItem == itemState.SWORD) {
            drawTextAction(g, "The chest had a", "sword. +" + SWORDATK + "ATK!", "");
        } else if (currItem == itemState.SHIELD) {
            drawTextAction(g, "The chest had a", "shield. +" + SHIELDDEF + "DEF!", "");
        }

        // print stats afterwards to account for item consumption
        g.drawString("HP:",772,150);
        g.drawString("" + thePlayer.getHP(), 910, 150);
        g.drawString("ATK:",772,228);
        g.drawString("" + thePlayer.getATK(), 910, 228);
        g.drawString("DEF:",772,308);
        g.drawString("" + thePlayer.getDEF(), 910, 308);
        g.drawString("Action:",713,443);

        // draw the board or combat screen
        inCombat();
        if (gamestate == menuState.PLAY) {
            drawBoard(g);
        } else if (gamestate == menuState.COMBAT) {
            drawCombat(g);
        }
    }

    // draws the drowning screen
    private void drawDrowned(Graphics g) {
        g.drawImage(splashes[splashcounter], 0, 0, this);
        if (splashcounter < SPLASHFRAMES - 1) {     // increment to 14, then stop.
            splashcounter++;
        }
    }

    // draws the death screen
    private void drawDied(Graphics g) {
        g.drawImage(deathies[deathcounter], 0, 0, this);
        if (deathcounter < DEATHFRAMES - 1) {     // increment to 14, then stop.
            deathcounter++;
        }
    }

    // draws text in the 'action' sidebar
    private void drawTextAction(Graphics g, String line1, String line2, String line3) {
        g.drawString(line1,713,483);
        g.drawString(line2,713,513);
        g.drawString(line3,713,543);
    }

    // draws the board, then the player ontop of it
    private void drawBoard(Graphics g) {
        for (int i = 0; i < BOARDWIDTH; i++) {
            for (int j = 0; j < BOARDHEIGHT; j++) {
                if (boardTiles[i][j] == 'g') {
                    g.drawImage(grass, i * TILEPIXELSIZE, j * TILEPIXELSIZE, this);
                } else if (boardTiles[i][j] == 't') {
                    g.drawImage(tree, i * TILEPIXELSIZE, j * TILEPIXELSIZE, this);
                } else if (boardTiles[i][j] == 'w') {
                    g.drawImage(water[frameNumber], i * TILEPIXELSIZE, j * TILEPIXELSIZE, this);
                } else if (boardTiles[i][j] == 'f') {
                    g.drawImage(flower[frameNumber], i * TILEPIXELSIZE, j * TILEPIXELSIZE, this);
                } else if (boardTiles[i][j] == 'r') {
                    g.drawImage(stone[frameNumber], i * TILEPIXELSIZE, j * TILEPIXELSIZE, this);
                } else if (boardTiles[i][j] == 's' || boardTiles[i][j] == 'd') {
                    g.drawImage(chest[frameNumber], i * TILEPIXELSIZE, j * TILEPIXELSIZE, this);
                } else if (boardTiles[i][j] >= '1' && boardTiles[i][j] <= '9') {
                    g.drawImage(enemy[frameNumber], i * TILEPIXELSIZE, j * TILEPIXELSIZE, this);
                } else if (boardTiles[i][j] == 'b') {
                    g.drawImage(boss[frameNumber], i * TILEPIXELSIZE, j * TILEPIXELSIZE, this);
                }
            }
        }

        if (boardTiles[thePlayer.getX()][thePlayer.getY()] == 'r') {    // if they are on a rock tile
            if (thePlayer.getDir() == UP) {
                g.drawImage(playerWaterUp[frameNumber], thePlayer.getX() * TILEPIXELSIZE, thePlayer.getY() * TILEPIXELSIZE, this);
            } else if (thePlayer.getDir() == DOWN) {
                g.drawImage(playerWaterDown[frameNumber], thePlayer.getX() * TILEPIXELSIZE, thePlayer.getY() * TILEPIXELSIZE, this);
            } else if (thePlayer.getDir() == RIGHT) {
                g.drawImage(playerWaterRight[frameNumber], thePlayer.getX() * TILEPIXELSIZE, thePlayer.getY() * TILEPIXELSIZE, this);
            } else if (thePlayer.getDir() == LEFT) {
                g.drawImage(playerWaterLeft[frameNumber], thePlayer.getX() * TILEPIXELSIZE, thePlayer.getY() * TILEPIXELSIZE, this);
            }
        } else {
            if (thePlayer.getDir() == UP) {                             // otherwise they are on a grass tile
                g.drawImage(playerIconUp[frameNumber], thePlayer.getX() * TILEPIXELSIZE, thePlayer.getY() * TILEPIXELSIZE, this);
            } else if (thePlayer.getDir() == DOWN) {
                g.drawImage(playerIconDown[frameNumber], thePlayer.getX() * TILEPIXELSIZE, thePlayer.getY() * TILEPIXELSIZE, this);
            } else if (thePlayer.getDir() == RIGHT) {
                g.drawImage(playerIconRight[frameNumber], thePlayer.getX() * TILEPIXELSIZE, thePlayer.getY() * TILEPIXELSIZE, this);
            } else if (thePlayer.getDir() == LEFT) {
                g.drawImage(playerIconLeft[frameNumber], thePlayer.getX() * TILEPIXELSIZE, thePlayer.getY() * TILEPIXELSIZE, this);
            }
        }
    }

    // draws the screen for when the player is in combat
    private void drawCombat(Graphics g) {
        // coordinates for cvarious enemies
        drawCoords pIdle = new drawCoords(45 + thePlayer.getOffset(), 203);
        drawCoords pAtk = new drawCoords(0 + thePlayer.getOffset(), 128);
        drawCoords pBlock = new drawCoords(45 + thePlayer.getOffset(), 203);
        drawCoords pBuff = new drawCoords(45 + thePlayer.getOffset(), 203);
        drawCoords pDam = new drawCoords(45 + thePlayer.getOffset(), 203);
        drawCoords eIdle = new drawCoords(340 + theEnemy.getOffset(), 203);
        drawCoords eAtk = new drawCoords(200 + theEnemy.getOffset(), 170);
        drawCoords eBlock = new drawCoords(200 + theEnemy.getOffset(), 170);
        drawCoords eBuff = new drawCoords(340 + theEnemy.getOffset(), 203);
        drawCoords eDam = new drawCoords(360 + theEnemy.getOffset(), 217);

        g.drawImage(battleArena, 0, 0, this);
        g.drawImage(battleBar, 350, 62, this);
        g.drawString("LVL: " + enemyLevel, 370, 100);
        g.drawString("HP: " + theEnemy.getHP(), 480, 100);
        
        // player selecting their option
        if (playerState == battleState.CHOOSE) {
            atkButton.setVisible(true);
            defButton.setVisible(true);
            buffButton.setVisible(true);
            thePlayer.drawIdle(g, pIdle.x(), pIdle.y());
            theEnemy.drawIdle(g, eIdle.x(), eIdle.y());
            return;
        }

        // player attack animation
        if (playerState == battleState.ATK) {
            thePlayer.drawAttack(g, pAtk.x(), pAtk.y());
            int damagedealt = damageCalc(thePlayer.getATK(), thePlayer.getBUFF(), theEnemy.getDEF());

            if (enemyState == battleState.BLOCK) {
                theEnemy.drawBlock(g, eBlock.x(), eBlock.y());
                damagedealt = 0;
                drawTextAction(g, "Your attack was", "blocked by the", "enemy!");
            } else {
                theEnemy.drawDamaged(g, eDam.x(), eDam.y());
                drawTextAction(g, "Your attack dealt", damagedealt + " damage!", "");
            }

            if (thePlayer.finishedAnim) {
                theEnemy.loseHP(damagedealt);
                playerState = battleState.DONE;
                if (enemyState == battleState.BLOCK) {
                    enemyState = battleState.CHOOSE;
                }

                if (theEnemy.getHP() <= 0) {
                    gamestate = menuState.PLAY;
                    playerState = battleState.CHOOSE;
                    numEnemies--;
                    checkAllEnemiesDead();
                }
            }
            return;
        }

        // enemy attack animation
        if (enemyState == battleState.ATK) {
            theEnemy.drawAttack(g, eAtk.x(), eAtk.y());
            int damagedealt = damageCalc(theEnemy.getATK(), theEnemy.getBUFF(), thePlayer.getDEF());

            if (playerState == battleState.BLOCK) {
                thePlayer.drawBlock(g, pBlock.x(), pBlock.y());
                damagedealt = 0;
                drawTextAction(g, "You blocked the", "enemy's attack!", "");
            } else {
                thePlayer.drawDamaged(g, pDam.x(), pDam.y());
                drawTextAction(g, "You took " + damagedealt, "damage from the", "enemy!");
            }

            if (theEnemy.finishedAnim) {
                thePlayer.loseHP(damagedealt);
                enemyState = battleState.CHOOSE;
                if (playerState == battleState.BLOCK) {
                    playerState = battleState.CHOOSE;
                }
            }
            return;
        }

        // play block animation
        if (playerState == battleState.BLOCK) {
            thePlayer.drawBlock(g, pBlock.x(), pBlock.y());
            theEnemy.drawIdle(g, eIdle.x(), eIdle.y());
            drawTextAction(g, "You blocked, but", "the enemy didn't", "attack!");
            if (thePlayer.finishedAnim) {
                playerState = battleState.DONE;
            }
            return;
        }

        // play enemy block animation
        if (enemyState == battleState.BLOCK) {
            theEnemy.drawBlock(g, eBlock.x(), eBlock.y());
            thePlayer.drawIdle(g, pIdle.x(), pIdle.y());
            drawTextAction(g, "The enemy tried", "to block, but you", "didn't attack!");
            if (theEnemy.finishedAnim) {
                enemyState = battleState.DONE;
            }
            return;
        }

        // play buff animation
        if (playerState == battleState.BUFF) {
            thePlayer.drawBuff(g, pBuff.x(), pBuff.y());
            theEnemy.drawIdle(g, eIdle.x(), eIdle.y());
            drawTextAction(g, "You gained an", "attack boost!", "1.5x base damage!");
            if (thePlayer.finishedAnim) {
                playerState = battleState.DONE;
                thePlayer.gainBUFF();
            }
            return;
        }

        // play enemy buff animation
        if (enemyState == battleState.BUFF) {
            thePlayer.drawIdle(g, pIdle.x(), pIdle.y());
            theEnemy.drawBuff(g, eBuff.x(), eBuff.y());
            drawTextAction(g, "Enemy buffed for", "an attack boost!", "1.5x base damage!");
            if (theEnemy.finishedAnim) {
                enemyState = battleState.DONE;
                theEnemy.gainBUFF();
            }
            return;
        }
       
        // if not displaying animations for player nor enemy
        checkDied();
        playerState = battleState.CHOOSE;
        enemyState = battleState.CHOOSE;
        thePlayer.drawIdle(g, pIdle.x(), pIdle.y());
        theEnemy.drawIdle(g, eIdle.x(), eIdle.y());
        return;
    }

    // GAMESTATE METHODS ----------------------------------------------------------------
    // returns the current state of the game
    public menuState getState() {
        return gamestate;
    }

    // sets the game into the start state
    public void startGame() {
        generateBoard();
        gamestate = menuState.PLAY;
        splashcounter = 0;
        deathcounter = 0;
    }

    // sets the gamestate to instruction mode
    public void instructUser() {
        gamestate = menuState.INSTRUCT;
    }

    // returns the user to the main menu
    public void goMenu() {
        gamestate = menuState.TITLE;
    }

    // prepares for combat
    private void prepCombat() {
        gamestate = menuState.COMBAT;
        thePlayer.resetBUFF();
        boardTiles[thePlayer.getX()][thePlayer.getY()] = 'g';
    }

    // MOVEMENT METHODS --------------------------------------------------------------------
    // moves the player down
    void movePlayerDown() {
        thePlayer.lookDown();
        if (invalidTile(thePlayer.getX(), thePlayer.getY() + 1)) {
            return;
        }
        thePlayer.moveDown();
        itemConsumed();
    }

    // moves the player up
    void movePlayerUp() {
        thePlayer.lookUp();
        if (invalidTile(thePlayer.getX(), thePlayer.getY() - 1)) {
            return;
        }
        thePlayer.moveUp();
        itemConsumed();
    }

    // moves the player left
    void movePlayerLeft() {
        thePlayer.lookLeft();
        if (invalidTile(thePlayer.getX() - 1, thePlayer.getY())) {
            return;
        }
        thePlayer.moveLeft();
        itemConsumed();
    }

    // moves the player right
    void movePlayerRight() {
        thePlayer.lookRight();
        if (invalidTile(thePlayer.getX() + 1, thePlayer.getY())) {
            return;
        }
        thePlayer.moveRight();
        itemConsumed();
    }

    // determines if a player drowned
    private void checkDrowned() {
        if (splashcounter == SPLASHFRAMES - 1) {
            gamestate = menuState.DROWNED_DONE;
            menuButton.setVisible(true);
        } else if (boardTiles[thePlayer.getX()][thePlayer.getY()] == 'w') {
            gamestate = menuState.DROWNED;
        }
    }

    // determines if a player was killed by an enemy
    private void checkDied() {
        if (deathcounter == DEATHFRAMES - 1) {
            gamestate = menuState.DEAD_DONE;
            menuButton.setVisible(true);
        } else if (thePlayer.getHP() <= 0) {
            gamestate = menuState.DEAD;
            playerState = battleState.CHOOSE;
        }
    }

    // determines if a player attempts to stand on an invalid tile (i.e a tree)
    private boolean invalidTile(int X, int Y) {
        if (X < 0 || Y < 0 || X == BOARDWIDTH || Y == BOARDHEIGHT) {
            return true;
        } else if (boardTiles[X][Y] == 't') {
            return true;
        }
        return false;
    }

    // determines if a player is in combat, if so, generates an enemy for them
    private void inCombat() {
        if (boardTiles[thePlayer.getX()][thePlayer.getY()] >= '1' && 
             boardTiles[thePlayer.getX()][thePlayer.getY()] <= '9') {
                int enemyLvl = boardTiles[thePlayer.getX()][thePlayer.getY()] - '0';
                theEnemy = new player(0, 0, enemyLvl * 5, enemyLvl, enemyLvl, "enemy");
                enemyLevel = String.valueOf(enemyLvl);
                prepCombat();

        } else if (boardTiles[thePlayer.getX()][thePlayer.getY()] == 'b') {
                int enemyLvl = 20;
                theEnemy = new player(0, 0, enemyLvl * 5, enemyLvl - 5, enemyLvl, "boss");  // attack nerf, bit too strong
                enemyLevel = "?";
                theEnemy.setOffset(-55);
                prepCombat();
        }
    }

    // COMBAT CLASS METHODS -----------------------------------------------------------------------
    // if the user clicks on the 'attack' button
    final ActionListener doAnAttack = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hideButtons();
            playerState = battleState.ATK;   
            enemyState = enemyChoose();   
        }
    };

    // if the user clicks on the 'block' button
    final ActionListener doABlock = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hideButtons();
            playerState = battleState.BLOCK;
            enemyState = enemyChoose();
        }
    };

    // if the user clicks on the 'buff' button
    final ActionListener doABuff = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hideButtons();
            playerState = battleState.BUFF;
            enemyState = enemyChoose();
        }
    };

    private void checkAllEnemiesDead() {
        if (numEnemies <= 0) {
            gamestate = menuState.VICTORY;
            menuButton.setVisible(true);
            return;
        }
    }
    
    // Item consumption methods -------------------------------------------------------------
    // determines if the player picked up an item
    private void pickedItem() {
        if (boardTiles[thePlayer.getX()][thePlayer.getY()] == 'f') {
            boardTiles[thePlayer.getX()][thePlayer.getY()] = 'g';
            thePlayer.gainHP(FLOWERHEAL);
            currItem = itemState.FLOWER;
            return;
        }

        if (boardTiles[thePlayer.getX()][thePlayer.getY()] == 's') {
            boardTiles[thePlayer.getX()][thePlayer.getY()] = 'g';
            thePlayer.gainATK(SWORDATK);
            currItem = itemState.SWORD;
            return;
        }

        if (boardTiles[thePlayer.getX()][thePlayer.getY()] == 'd') {
            boardTiles[thePlayer.getX()][thePlayer.getY()] = 'g';
            thePlayer.gainDEF(SHIELDDEF);
            currItem = itemState.SHIELD;
        }
    }
    
    // removes the status of all consumed items
    private void itemConsumed() {
        currItem = itemState.NONE;
    }

    // combat helper methods ----------------------------------------------------------------------
    // hides the buttons in the combat menu
    private void hideButtons() {
        atkButton.setVisible(false);
        defButton.setVisible(false);
        buffButton.setVisible(false);
    }

    // has the enemy make a choice
    private battleState enemyChoose() {
        int numChoices = 3;
        int rand = (int)(Math.random() * numChoices);

        if (rand == 0) {
            return battleState.BLOCK;
        } else if (rand == 1) {
            return battleState.ATK;
        } else {
            return battleState.BUFF;
        }
    }

    // calculates how much damage is dealt to an enemy
    // if the defense stat is greater than the attack, no damage is dealt
    private int damageCalc(int atkAmt, int atkMult, int defAmt) {
        Double atk = Double.valueOf(atkAmt);
        Double buff = Double.valueOf(atkMult);
        Double def = Double.valueOf(defAmt);
  
        atk = atk * (1.0 + buff) / 2.0;
        int amt = (int)(atk + atk / def);
        if (amt > 0) {
            return amt;
        }
        return 0;
    }

    // board initialization methods ---------------------------------------------------------------
    // generates the board by parsing a CSV file for the map
    // determines player spawn location and number of enemies - player default spawns at (1, 1)
    private void generateBoard() {
        numEnemies = 0;
        int playerXSpawn = 1;
        int playerYSpawn = 1;

        try {
            Scanner sc = new Scanner(new File(mapFile));    
            sc.useDelimiter(",");

            for (int i = 0; i < BOARDHEIGHT; i++) {
                for (int j = 0; j < BOARDWIDTH; j++) {
                    boardTiles[j][i] = sc.next().charAt(0);

                    if ((boardTiles[j][i] >= '0' && boardTiles[j][i] <= '9') || boardTiles[j][i] == 'b') {
                        numEnemies++;

                    } else if (boardTiles[j][i] == 'p') {
                        playerXSpawn = j;
                        playerYSpawn = i;
                        boardTiles[j][i] = 'g';
                    }
                }
                sc.nextLine();      // SKIP OVER NEWLINE CHAR
            }
            sc.close();

        } catch (FileNotFoundException ex) {
            System.out.println("Error! The map \"" +  mapFile + "\" was not found");
            ex.printStackTrace();
        }
        thePlayer = new player(playerXSpawn, playerYSpawn, startingHP, startingATK, startingDEF, "player");
    }

    // grab the images - static images first, then the ones with animation frames
    private void loadImage() {
        ImageIcon ts = new ImageIcon("images/backgrounds/titlescreen.jpg");
        ImageIcon is = new ImageIcon("images/backgrounds/instructions.jpg");
        ImageIcon vs = new ImageIcon("images/backgrounds/victoryscreen.jpg");
        ImageIcon ss = new ImageIcon("images/backgrounds/sidebar.jpg");
        ImageIcon ba = new ImageIcon("images/backgrounds/battlearena.jpg");
        ImageIcon tt = new ImageIcon("images/tiles/tree.jpg");
        ImageIcon gg = new ImageIcon("images/tiles/grass.jpg");
        ImageIcon bb = new ImageIcon("images/buttons/button.png");

        titlescreen = ts.getImage();
        instructscreen = is.getImage();
        victoryscreen = vs.getImage();
        grass = gg.getImage();
        tree = tt.getImage();
        sidebar = ss.getImage();
        battleArena = ba.getImage();
        battleBar = bb.getImage();

        for (int i = 0; i < TILEFRAMES; i++) {
            ImageIcon ww = new ImageIcon("images/tiles/water" + i + ".jpg");
            ImageIcon en = new ImageIcon("images/tiles/skeleton" + i + ".jpg");
            ImageIcon bs = new ImageIcon("images/tiles/boss" + i + ".jpg");
            ImageIcon st = new ImageIcon("images/tiles/rock" + i + ".jpg");
            ImageIcon ff = new ImageIcon("images/tiles/flower" + i + ".jpg");
            ImageIcon ch = new ImageIcon("images/tiles/chest" + i + ".jpg");

            ImageIcon ppN = new ImageIcon("images/tiles/playerup" + i + ".jpg");
            ImageIcon ppS = new ImageIcon("images/tiles/playerdown" + i + ".jpg");
            ImageIcon ppE = new ImageIcon("images/tiles/playerright" + i + ".jpg");
            ImageIcon ppW = new ImageIcon("images/tiles/playerleft" + i + ".jpg");
            ImageIcon pwN = new ImageIcon("images/tiles/playerupw" + i + ".jpg");
            ImageIcon pwS = new ImageIcon("images/tiles/playerdownw" + i + ".jpg");
            ImageIcon pwE = new ImageIcon("images/tiles/playerrightw" + i + ".jpg");
            ImageIcon pwW = new ImageIcon("images/tiles/playerleftw" + i + ".jpg");

            water[i] = ww.getImage();
            enemy[i] = en.getImage();
            boss[i] = bs.getImage();
            stone[i] = st.getImage();
            flower[i] = ff.getImage();
            chest[i] = ch.getImage();

            playerIconUp[i] = ppN.getImage();
            playerIconDown[i] = ppS.getImage();
            playerIconRight[i] = ppE.getImage();
            playerIconLeft[i] = ppW.getImage();
            playerWaterUp[i] = pwN.getImage();
            playerWaterDown[i] = pwS.getImage();
            playerWaterRight[i] = pwE.getImage();
            playerWaterLeft[i] = pwW.getImage();
        }

        // for drown effect
        for (int i = 0; i < SPLASHFRAMES; i++) {
            ImageIcon tmp = new ImageIcon("images/backgrounds/splash" + i + ".jpg");
            splashes[i] = tmp.getImage();
        }

        for (int i = 0; i < DEATHFRAMES; i++) {
            ImageIcon tmp = new ImageIcon("images/backgrounds/deathscreen" + i + ".jpg");
            deathies[i] = tmp.getImage();
        }
    }
}
