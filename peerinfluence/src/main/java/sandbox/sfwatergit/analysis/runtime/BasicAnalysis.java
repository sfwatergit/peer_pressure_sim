package sandbox.sfwatergit.analysis.runtime;

import com.google.common.collect.Lists;
import gnu.trove.TDoubleArrayList;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import sandbox.sfwatergit.analysis.charts.PressuredPeopleChartWriter;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.PressureDataManager;
import sandbox.sfwatergit.utils.postgresql.CSVWriter;
import sandbox.sfwatergit.utils.postgresql.PostgresType;
import sandbox.sfwatergit.utils.postgresql.PostgresqlColumnDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is a module that provides in-scenario travelTime functionality
 * <p>
 * Created by sidneyfeygin on 7/28/15.
 */
public class BasicAnalysis {

    private final Scenario scenario;
    private final OutputDirectoryHierarchy controlerIO;
    private final TDoubleArrayList iterations;
    private final Collection<PressureDataManager.IterationDataRecord> pressureRecords;
    private final PressuredPeopleChartWriter pressuredPeopleChartWriter;


    public BasicAnalysis(Scenario scenario, OutputDirectoryHierarchy controlerIO, TDoubleArrayList iterations, Collection<PressureDataManager.IterationDataRecord> pressureRecords) {
        this.scenario = scenario;
        this.controlerIO = controlerIO;
        this.iterations = iterations;
        this.pressureRecords = pressureRecords;
        pressuredPeopleChartWriter = new PressuredPeopleChartWriter("", "Iteration", "People To Pressure");
    }

    public void writePressureDataToFile(int popSize){

        TDoubleArrayList peoplePressuredArray = pressuredPersonAnalysis.run(pressureRecords);
        final List<Map<String, Double>> modeShareList = modeShareAnalysis.run(pressureRecords);
        List<PostgresqlColumnDefinition> columnDefs = Lists.newArrayList();

        columnDefs.add(new PostgresqlColumnDefinition("Iteration", PostgresType.BIGINT));

        final List<String> modes = modeShareList.get(0).keySet().stream().sorted().collect(Collectors.toList());
        int numModes = modes.size();
        columnDefs.add(new PostgresqlColumnDefinition("PeoplePressured", PostgresType.BIGINT));
        modes.stream().sorted().forEach((mode)-> columnDefs.add(new PostgresqlColumnDefinition(String.format("%sAgents", mode), PostgresType.BIGINT)));

        CSVWriter csvWriter = new CSVWriter(String.format("%s_IterPressureModeData", scenario.getConfig().controler().getRunId()), controlerIO.getOutputPath(),20,columnDefs);

        csvWriter.init();

        for(int i = 0;i<iterations.size();i++){

            String[] data = new String[2+numModes];
            data[0] = String.valueOf(i);
            data[1]= String.valueOf(peoplePressuredArray.get(i));

            final Map<String, Double> iterModeData = modeShareList.get(i);
            for (int j = 0; j < numModes; j++) {
                data[j+2]=String.valueOf(Optional.ofNullable(iterModeData.get(modes.get(j))).orElse(0.0)*popSize/100.0);
            }
            csvWriter.addLine(data);

        }
        csvWriter.finish();

    }


    public void writeChart() {

        TDoubleArrayList peoplePressuredArray = pressuredPersonAnalysis.run(pressureRecords);

        List<Map<String,Double>> modeShareMap = pressureRecords.stream().map(PressureDataManager.IterationDataRecord::getIterationModeShareData).collect(Collectors.toList());

        pressuredPeopleChartWriter.writeChart(iterations, peoplePressuredArray,modeShareMap,controlerIO.getOutputFilename("pressureChart.png"));

    }


    public static IterationRecordAnalysisTask<TDoubleArrayList> pressuredPersonAnalysis = (recs)-> new TDoubleArrayList(recs.stream().map(PressureDataManager.IterationDataRecord::getNumPeoplePressured).mapToDouble(p->(double)p).toArray());


    public static IterationRecordAnalysisTask<List<Map<String,Double>>> modeShareAnalysis = (recs)-> recs.stream().map(PressureDataManager.IterationDataRecord::getIterationModeShareData).collect(Collectors.toList());

    @FunctionalInterface
    public interface IterationRecordAnalysisTask<T>{

        T run(Collection<PressureDataManager.IterationDataRecord> dataRecords);
    }


}
