package rpggamev2;

// Import a bunch-o-libraries for playing audio!
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

// plays various soundtracks
public class audioPlayer {
    // define the songs used
    private final String MENU_THEME = "./music/menu_theme.wav";
    private final String OVERWORLD_THEME = "./music/overworld_theme.wav";
    private final String BATTLE_THEME = "./music/battle_theme.wav";
    private final String VICTORY_THEME =  "./music/victory_theme.wav";
    private final String DEATH_THEME = "./music/death_theme.wav";
    private final String DROWN_THEME = "./music/drown_theme.wav";

    // define variables
    private boolean init = false;
    private Clip audioClip;

    // initializer
    audioPlayer() {
        playMenuTheme();
        init = true;
    }

    // public methods for playing the various soundtracks for e n c a p s u l a t i o n -----------
    public void playOverworldTheme() {
        playTrack(OVERWORLD_THEME);
    }

    public void playMenuTheme() {
        playTrack(MENU_THEME);
    }

    public void playBattleTheme() {
        playTrack(BATTLE_THEME);
    }

    public void playVictoryTheme() {
        playTrack(VICTORY_THEME);
    }

    public void playDeathTheme() {
        playTrack(DEATH_THEME);
    }

    public void playDrownTheme() {
        playTrack(DROWN_THEME);
    }

    // helper function for playing the various songs ----------------------------------------------
    private void playTrack(String filename) {
        if (init) {     // stop the audio clip if it currently exists to avoid overlap
            audioClip.stop();
        }

        try {
            File audioFile = new File(filename);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();   
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            audioClip = (Clip) AudioSystem.getLine(info);
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
}
