package sandbox.sfwatergit.analysis.stats;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.matsim.api.core.v01.population.Person;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm.PressurePersonData;

import java.util.Collection;

/**
 * General interface to use with any sort of pressure statistic
 * <p>
 * Created by sidneyfeygin on 12/2/15.
 */
@FunctionalInterface
public interface PressureStatAggregator<T, E extends Enum<E>> {

    /**
     * Compute {@link DescriptiveStatistics} from the given {@link PressurePersonData}
     *
     * @param ppds input dataset for one {@link Person}
     * @return the result of the computation
     */
    T getStatistics(Collection<PressurePersonData> ppds, E statKind);

}
