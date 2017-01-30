package sandbox.sfwatergit.analysis.stats;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.core.utils.io.IOUtils;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.PressureDataManager;
import sandbox.sfwatergit.utils.obj.PredicateUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Functional factory and utility for pressure statistics.
 * <p>
 * Created by sidneyfeygin on 12/2/15.
 */
public class PressureAnalysisUtils {

    public static Map<StatKind, PressureStatAggregator<DescriptiveStatistics, StatKind>> STAT_CONVERTER_SUPPLIER_MAP = Maps.newHashMap();

    @Inject
    public PressureAnalysisUtils() {
        Map<StatKind, PressureStatAggregator<DescriptiveStatistics, StatKind>> tempMap = Maps.newHashMap();
        Map<HistKind, PressureStatAggregator<LinearDiscretizer, HistKind>> tempMap2 = Maps.newHashMap();

        for (StatKind statKind : StatKind.values()) {
            tempMap.put(statKind,
                    (ppds, kind) -> new DescriptiveStatistics(
                            ppds.stream().filter(PredicateUtils.notNull)
                                    .mapToDouble(ppd ->
                                            Optional.ofNullable(ppd.getStat(kind)).orElse(0.0))
                                    .filter(Double::isFinite).toArray()));
        }


        for (HistKind histKind : HistKind.values()) {
            tempMap2.put(histKind,
                    (ppds, kind) -> new LinearDiscretizer(
                            ppds.stream().mapToDouble(ppd -> ppd.getHist(kind))
                                    .filter(Double::isFinite).toArray(), 10));


            STAT_CONVERTER_SUPPLIER_MAP = tempMap;
        }
    }

    public static void writeDescriptiveStats(String outputDir, final TreeMap<Integer, PressureDataManager.IterationDataRecord> statsHistory) {

        final List<Map<StatKind, DescriptiveStatistics>> iterStatHistory = statsHistory.values().stream().map(PressureDataManager.IterationDataRecord::getIterationStatsHistory).collect(Collectors.toList());


        try {
            for (StatKind statKind : StatKind.values()) {
                final Iterator<Integer> iterIterator = statsHistory.navigableKeySet().iterator();
                final String outputFilename = String.format("%s_History.csv", outputDir + statKind.toString().toLowerCase());
                final BufferedWriter br = IOUtils.getBufferedWriter(outputFilename);
                br.write("iteration\tmean\tmedian\tmax\tn\tmin\tvariance\ttotal");
                br.newLine();
                for (Map<StatKind, DescriptiveStatistics> stathist : iterStatHistory) {


                    final DescriptiveStatistics descriptiveStat = stathist.get(statKind);
                    Double[] stats = new Double[]{descriptiveStat.getMean(), descriptiveStat.getPercentile(50), descriptiveStat.getMax(), Double.valueOf(descriptiveStat.getN()), descriptiveStat.getMin(), descriptiveStat.getVariance(), descriptiveStat.getSum()};
                    br.write(String.valueOf(iterIterator.next()));
                    br.write("\t");
                    br.write(Joiner.on('\t').join(stats));
                    br.newLine();
                    br.flush();
                }

                br.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
