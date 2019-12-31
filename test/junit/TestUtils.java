import java.io.File;
import java.io.FileNotFoundException;
import org.junit.Ignore;

@Ignore
public class TestUtils {

  public static final String INPUT_EXTENSION = ".java";
  public static final String EXPECTED_EXTENSION = ".expected";

  // hack because ant and intellij don't play nicely together
  public static String getTestResources() {
    File f = new File("resources");
    if (f.exists()) {
      return "resources";
    } else {
      return "test/resources";
    }
  }

  public static String getProjectDir() {
    String res = System.getProperty("user.dir");
    // ASSUMES you don't have "test" in your file path before the test dir in
    // this project!
    if (!res.contains("test")) {
      return res + "/";
    }
    return res.substring(0, res.indexOf("test"));
  }

}
