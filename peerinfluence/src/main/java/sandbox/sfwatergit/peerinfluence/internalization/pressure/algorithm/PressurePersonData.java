package sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import sandbox.sfwatergit.analysis.stats.HistKind;
import sandbox.sfwatergit.analysis.stats.StatKind;
import sandbox.sfwatergit.peerinfluence.internalization.ExternalityType;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.matsim.api.core.v01.TransportMode.car;
import static org.matsim.api.core.v01.TransportMode.pt;


/**
 * Pressure data
 * <p>
 * Created by sidneyfeygin on 7/25/15.
 */
@Data
public class PressurePersonData implements PressureData {

    private Map<String, Double> bestPlanScores = this.getDefaultPlanScore();
    private EnumMap<ExternalityType, Double> externality = this.getDefaultExternality();
    private Map<Integer,String> modeHistoryMap=Maps.newHashMap();
    private TreeMap<Integer,Boolean> pressureHistoryMap = Maps.newTreeMap();

    private double externalityPenalty=Double.NaN;
    private Double selectedPlanScore=Double.NaN;
    private Double pressurePenalty=0.0;
    private Set<SocialVertex> pressuredBy=Sets.newHashSet();

    public PressurePersonData() {
    }

    public void setShifted(int iteration, boolean wasPressured) {
        pressureHistoryMap.put(iteration,wasPressured);
    }

    public boolean wasPressured(int iteration){
        if(!pressureHistoryMap.isEmpty())
            return pressureHistoryMap.get(iteration);
        else
            return false;
    }

    public void addToPressurePenalty(double pressurePenalty){
        this.pressurePenalty+=pressurePenalty;
    }

    public double setExternality(EnumMap<ExternalityType,Double> externalities){
        this.externality = externalities;
        return externalities.values().stream().mapToDouble(Double::doubleValue).sum();
    }


    public Double getStat(StatKind kind) {
        switch (kind) {
            case CONGESTION: {
                return externality.get(ExternalityType.CONGESTION);
            }
            case GHG: {
                return externality.get(ExternalityType.GHG);
            }
            case UTILITYDIFF: {
                return getUtilityDiff();
            }
            default:
                return Double.NaN;
        }
    }

    public Double getUtilityDiff() {
        return this.bestPlanScores.get(car)-this.bestPlanScores.get(pt);
    }

    public Double getHist(HistKind kind) {
        switch (kind) {
            case SELECTED_SCORE: {
                return getSelectedPlanScore();
            }
            case LOGSUM: {
//                return computeLogSum();
            }
            default:
                return Double.NaN;
        }
    }



    public void putModeData(int iteration, String modeOfSelectedPlan) {
        this.modeHistoryMap.put(iteration, modeOfSelectedPlan);
    }

    public String getModeShift(int iteration) {
        final String currMode = this.modeHistoryMap.get(iteration);
        final String prevMode = this.modeHistoryMap.getOrDefault(iteration - 1, "NA");
        return String.format("%s_to_%s",prevMode,currMode);
    }


    public void clearPressurePenalty() {
        this.pressurePenalty=0.0;
    }
}
