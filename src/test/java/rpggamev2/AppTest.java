package rpggamev2;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.io.File;

// unit test for App
public class AppTest {

     // verify environment was setup correctly for compilation assertions
    @Test
    public void assertTest() {
        assertTrue(true);
        System.out.println("Basic assertion test passed!");
    }

    // verify image, audio, and map assets are in the correct directories
    @Test
    public void checkImages() {
        assert(checkPath("/images"));
        System.out.println("Images are in the correct directory!");
    }

    @Test
    public void checkAudio() {
        assert(checkPath("/music"));
        System.out.println("Audio files are in the correct directory!");
    }

    @Test public void checkMap() {
        assert(checkPath("/map1.csv"));
        System.out.println("Map is in the correct directory!");
    }

    private boolean checkPath(String thePath) {
        File tmpDir = new File("./target" + thePath);
        return tmpDir.exists();
    }
}
