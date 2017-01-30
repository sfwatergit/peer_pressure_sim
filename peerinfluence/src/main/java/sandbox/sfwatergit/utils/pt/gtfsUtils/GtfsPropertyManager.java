package sandbox.sfwatergit.utils.pt.gtfsUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static sandbox.sfwatergit.utils.pt.gtfsUtils.GTFSConstants.PROP_FILE_PATH;

/**
 * Centralizes reading and writing of properties and defaults
 *
 * Created by sfeygin on 11/13/16.
 */
public class GtfsPropertyManager {
    private String opMapPath;
    private String apiKey;
    private String outputDir;

    public GtfsPropertyManager() {
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream(PROP_FILE_PATH);
            props.load(in);
            opMapPath = (String) props.get("gtfs.opFileName");
            apiKey = (String) props.get("gtfs.apiKey");
            outputDir = (String) props.get("gtfs.outputDir");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOpMapPath() {
        return opMapPath;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getOutputDir() {
        return outputDir;
    }
}
