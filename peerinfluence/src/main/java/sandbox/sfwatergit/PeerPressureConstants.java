package sandbox.sfwatergit;

import java.util.function.Function;

/**
 * Purpose of Class:
 * <p>
 * Date: 3/15/15
 * Time: 9:03 AM
 * Version: 1.0
 *
 * TODO: Read from config
 *
 * @author sfwatergit
 */
public final class PeerPressureConstants {
    public static String SC_CRS = "epsg:26910";
    public static String OUTPUT_DIR = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/output/sf_bay";
    public static String INPUT_DIR = "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/input/sf_bay";
    String RES_DIR = "/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources";

    private PeerPressureConstants(){}



    public static Function<String, String> getInDir = (s) -> String.format("%1$s%2$s", INPUT_DIR, s);
    public static Function<String, String> getOutDir = (s) -> String.format("%1$s%2$s", OUTPUT_DIR, s);
}
