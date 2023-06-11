package rpggamev2;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;

public class player {
    private int playerX;
    private int playerY;
    private int playerHP;
    private int playerATK;
    private int playerDEF;
    private int playerBUFF;
    private char playerDir = 'e'; // Must be one of: N, S, E, W
    private int playerOffset = 0;

    private final int IDLEFRAMES = 6;
    private final int FRAMEDELAY = 4;

    private Image[] idleAnim = new Image[IDLEFRAMES];
    private Image[] attackAnim = new Image[IDLEFRAMES];
    private Image[] blockAnim = new Image[IDLEFRAMES];
    private Image[] buffAnim = new Image[IDLEFRAMES];
    private Image[] damageAnim = new Image[IDLEFRAMES];
    private int frameNum = 0;
    private int frameHelper = 0;
    private boolean startedAnim = false;
    public boolean finishedAnim = false;

    // player initialization at coords x, y
    player(int x, int y, int HP, int ATK, int DEF, String assetName) {
        playerX = x;
        playerY = y;
        playerHP = HP;
        playerATK = ATK;
        playerDEF = DEF;
        playerBUFF = 1;

        for (int i = 0; i < IDLEFRAMES; i++) {
            ImageIcon ii = new ImageIcon("images/" + assetName + "/idle" + i + ".png");
            idleAnim[i] = ii.getImage();

            ImageIcon ai = new ImageIcon("images/" + assetName + "/attack" + i + ".png");
            attackAnim[i] = ai.getImage();

            ImageIcon bi = new ImageIcon("images/" + assetName + "/block" + i + ".png");
            blockAnim[i] = bi.getImage();

            ImageIcon bf = new ImageIcon("images/" + assetName + "/buff" + i + ".png");
            buffAnim[i] = bf.getImage();

            ImageIcon dm = new ImageIcon("images/" + assetName + "/damaged" + i + ".png");
            damageAnim[i] = dm.getImage();
        }
    }

    // obtain player stats ---------------------------------
    int getX() {
        return playerX;
    }
    int getY() {
        return playerY;
    }
    int getHP() {
        return playerHP;
    }
    int getATK() {
        return playerATK;
    }
    int getDEF() {
        return playerDEF;
    }
    int getBUFF() {
        return playerBUFF;
    }
    int getOffset() {
        return playerOffset;
    }

    // movement methods ------------------------------------
    void moveUp() {
        playerY = playerY - 1;
    }
    void moveDown() {
        playerY = playerY + 1;
    }
    void moveLeft() {
        playerX = playerX - 1;
    }
    void moveRight() {
        playerX = playerX + 1;
    }

    // USE NSEW DIRECTIONS
    void lookUp() {
        playerDir = 'n';
    }
    void lookDown() {
        playerDir = 's';
    }
    void lookLeft() {
        playerDir = 'w';
    }
    void lookRight() {
        playerDir = 'e';
    }
    char getDir() {
        return playerDir;
    }

    // stat change methods -----------------------------------
    void loseHP(int X) {
        playerHP = playerHP - X;
        if (playerHP < 0) {
            playerHP = 0;
        }
    }
    void gainHP(int X) {
        playerHP = playerHP + X;
    }
    void gainATK(int X) {
        playerATK = playerATK + X;
    }
    void gainDEF(int X) {
        playerDEF = playerDEF + X;
    }
    void gainBUFF() {
        playerBUFF++;
    }
    void resetBUFF() {
        playerBUFF = 1;
    }
    void setOffset(int X) {
        playerOffset = X;
    }

    // animation methods ----------------------------------------
    // draws the idle animation
    void drawIdle(Graphics g, int X, int Y) {
        g.drawImage(idleAnim[frameNum], X, Y, null);
        frameHelper++;
        frameHelper = frameHelper % (FRAMEDELAY * IDLEFRAMES);

        if (frameHelper % FRAMEDELAY == 0) {
            frameNum++;
            frameNum = frameNum % IDLEFRAMES;
        }
    }

    // has the user do their attack animation
    void drawAttack(Graphics g, int X, int Y) {
        startAnim();
        g.drawImage(attackAnim[frameNum], X, Y, null);
        incrementFrames();
    }

    // has the user do their block animation
    void drawBlock(Graphics g, int X, int Y) {
        startAnim();
        g.drawImage(blockAnim[frameNum], X, Y, null);      
        incrementFrames();
    }

    // has the user do their buff animation
    void drawBuff(Graphics g, int X, int Y) {
        startAnim();
        g.drawImage(buffAnim[frameNum], X, Y, null);
        incrementFrames();
    }

    void drawDamaged(Graphics g, int X, int Y) {
        startAnim();
        g.drawImage(damageAnim[frameNum], X, Y, null);
        incrementFrames();
    }

    // resets counters to 0 to fully display non-idle animations 
    private void startAnim() {
        if (!startedAnim) {
            startedAnim = true;
            frameNum = 0;
            frameHelper = 0;
            finishedAnim = false;
        }
    }

    // increments the frames for playing non-idle animations
    private void incrementFrames() {
        frameHelper++;
        if (frameHelper % FRAMEDELAY == 0) {
            frameNum++;
        }
        if (frameHelper == (FRAMEDELAY * IDLEFRAMES)) {
            frameHelper = 0;
            frameNum = 0;
            startedAnim = false;
            finishedAnim = true;
        }
    }
}
