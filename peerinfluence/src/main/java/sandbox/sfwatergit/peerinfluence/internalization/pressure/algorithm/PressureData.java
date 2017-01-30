package sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm;

import com.google.common.collect.Maps;
import org.matsim.api.core.v01.TransportMode;
import sandbox.sfwatergit.peerinfluence.internalization.ExternalityType;

import java.util.EnumMap;
import java.util.Map;

import static org.matsim.api.core.v01.TransportMode.pt;

/**
 * Created by sidneyfeygin on 12/12/15.
 */
public interface PressureData {

    default Map<String, Double> getDefaultPlanScore() {
        final Map<String, Double> bestPlanDefaultMap = Maps.newHashMap();
        bestPlanDefaultMap.put(TransportMode.car, Double.NEGATIVE_INFINITY);
        bestPlanDefaultMap.put(pt, Double.NEGATIVE_INFINITY);
        return bestPlanDefaultMap;
    }

    default EnumMap<ExternalityType, Double> getDefaultExternality() {
        final EnumMap<ExternalityType, Double> result = new EnumMap<>(ExternalityType.class);
        result.put(ExternalityType.GHG, 0.0);
        result.put(ExternalityType.CONGESTION, 0.0);
        return result;
    }



}
