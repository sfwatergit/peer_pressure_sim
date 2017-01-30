package sandbox.sfwatergit.utils.pt.gtfsUtils.ptMatrix;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Utility needed to convert format of OTPMatrixRouter output to matrixbasedptrouter input
 * <p>
 * Created by sfeygin on 11/15/16.
 */
public class StopToIntConverter {

    private static final Logger log = Logger.getLogger(PTMatrixCreator.class);
    private final CSVFileWriter stopsWriter;
    private final CSVFileWriter tdWriter;
    private final CSVFileWriter ttWriter;
    private static final String STOPS_SEP = ",";
    private static final String TT_TD_SEP = " ";
    Map<String, Long> stopFacs2Idx;

    public StopToIntConverter(String outputFileStops,
                              String outputFileTt,
                              String outputFileTd) {
        this.stopsWriter = new CSVFileWriter(outputFileStops, STOPS_SEP);
        this.ttWriter = new CSVFileWriter(outputFileTt, TT_TD_SEP);
        this.tdWriter = new CSVFileWriter(outputFileTd, TT_TD_SEP);
        this.stopFacs2Idx = Maps.newHashMap();
    }

    public void mapStopsToInts(String inputFileStops) {
        Long idx = 0L;
        stopsWriter.writeField("id");
        stopsWriter.writeField("x");
        stopsWriter.writeField("y");
        stopsWriter.writeNewLine();

        final CSVReader stopsReader = new CSVReader(inputFileStops, STOPS_SEP);

        stopsReader.readLine();
        String[] line = stopsReader.readLine();
        while (line != null) {
            String stop = line[0];
            stopFacs2Idx.put(stop,idx);
            writeStop(idx, line[1], line[2]);
            line = stopsReader.readLine();
            idx+=1;
        }
        stopsWriter.close();
        log.info("Stops file based on schedule written.");
    }

    private void mapTravelData(String inputFileTd, String inputFileTt) {
        final CSVReader tdReader = new CSVReader(inputFileTd, TT_TD_SEP);
        final CSVReader ttReader = new CSVReader(inputFileTt, TT_TD_SEP);

        String[] tdLine = tdReader.readLine();
        String[] ttLine = ttReader.readLine();
        while (tdLine != null && ttLine != null) {
            String stopFrom = tdLine[0];
            String stopTo = tdLine[1];
            final Long stopFromIdx = stopFacs2Idx.get(stopFrom);
            final Long stopToIdx = stopFacs2Idx.get(stopTo);
            writeTravelData(stopFromIdx,stopToIdx,ttLine[2],tdLine[2]);
            tdLine = tdReader.readLine();
            ttLine = ttReader.readLine();
        }

        tdWriter.close();
        log.info("Travel distance file converted.");
        ttWriter.close();
        log.info("Travel time file converted.");


    }


    private void writeStop(long stopIdx, String x, String y) {
        stopsWriter.writeField(stopIdx);
        stopsWriter.writeField(x);
        stopsWriter.writeField(y);
        stopsWriter.writeNewLine();
    }

    private void writeTravelData(long stopIdxFrom, long stopIdxTo, String tt, String td) {

        tdWriter.writeField(stopIdxFrom);
        tdWriter.writeField(stopIdxTo);
        tdWriter.writeField(td);
        tdWriter.writeNewLine();

        ttWriter.writeField(stopIdxFrom);
        ttWriter.writeField(stopIdxTo);
        ttWriter.writeField(tt);
        ttWriter.writeNewLine();

    }

    public static void main(String[] args) {
        final StopToIntConverter converter = new StopToIntConverter("/Users/sfeygin/current_code/java/research/ucb_smartcities_all/output/sf_bay/gtfs/ptStops1.csv",
                "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/output/sf_bay/gtfs/tt1.csv",
                "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/output/sf_bay/gtfs/td1.csv");

        converter.mapStopsToInts("/Users/sfeygin/current_code/java/research/ucb_smartcities_all/output/sf_bay/gtfs/ptStops.csv");

        converter.mapTravelData("/Users/sfeygin/current_code/java/research/ucb_smartcities_all/output/sf_bay/gtfs/td.csv",
                "/Users/sfeygin/current_code/java/research/ucb_smartcities_all/output/sf_bay/gtfs/tt.csv");

    }

}
