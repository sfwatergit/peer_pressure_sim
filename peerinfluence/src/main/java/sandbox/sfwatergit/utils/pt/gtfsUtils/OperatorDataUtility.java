package sandbox.sfwatergit.utils.pt.gtfsUtils;

import org.apache.log4j.Logger;
import org.matsim.core.utils.io.IOUtils;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import sandbox.sfwatergit.utils.obj.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Takes care of retrieving {@link Operator} data.
 *
 * Created by sfeygin on 11/11/16.
 */
public class OperatorDataUtility {

    private static final Logger log = Logger.getLogger(TransitDataDownloader.class);
    private String opMapPath;
    private String apiKey;

    public OperatorDataUtility(){
        GtfsPropertyManager props = new GtfsPropertyManager();
        opMapPath=props.getOpMapPath();
        apiKey=props.getApiKey();
    }


    public Map<String, String> getOperatorMap() {
        Map<String, String> operatorMap;
        if (new File(opMapPath).exists()){
            operatorMap = readOperatorMapFromFile(opMapPath);
        } else{
            log.info("Operator key file not found. Downloading and saving...");
            operatorMap = downloadOperatorMap(apiKey);
            saveOperatorMap(opMapPath, operatorMap);
        }
        return operatorMap;
    }


    private void saveOperatorMap(String opMapPath, Map<String,String> operatorMap) {
        try {
            CsvMapWriter csvMapWriter = new CsvMapWriter(IOUtils.getBufferedWriter(opMapPath), CsvPreference.STANDARD_PREFERENCE);
            final String[] opKeyArray = operatorMap.keySet().stream().toArray(String[]::new);
            csvMapWriter.writeHeader(opKeyArray);
            csvMapWriter.write(operatorMap, opKeyArray);
            csvMapWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(String.format("Operator key file saved at %s", opMapPath));
    }

    @NotNull
    private Map<String, String> readOperatorMapFromFile(String opMapPath) {
        CsvMapReader mapReader = new CsvMapReader(IOUtils.getBufferedReader(opMapPath), CsvPreference.STANDARD_PREFERENCE);
        final String[] header;
        Map<String, String> res = null;
        try {
            header = mapReader.getHeader(true);
            res = mapReader.read(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private Map<String, String> downloadOperatorMap(String apiKey) {
        final TransitDataDownloader downloader = TransitDataDownloader.getInstance(apiKey);
        List<Operator> transitOperatorList = downloader.getTransitOperatorList();
        return transitOperatorList.stream().distinct().collect(Collectors.toMap(Operator::getName, Operator::getPrivateCode));
    }


}
