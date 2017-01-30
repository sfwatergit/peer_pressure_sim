package sandbox.sfwatergit.peerinfluence.run.config;

import com.google.common.base.Joiner;
import org.matsim.core.config.ReflectiveConfigGroup;

/**
 * Created by sidneyfeygin on 7/28/15.
 */
public class PeerPressureAnalysisConfigGroup extends ReflectiveConfigGroup {

    public static final String GROUP_NAME = "peerPressure";
    public static final String MODE_TYPES = "modeTypes";
    public static final String START_PRESSURE_ITERATION = "startIteration";
    public static final String PRESSURE_COST = "pressureCost";

    // Private fields and default values
    private String[] modeTypes = new String[]{"car", "pt"};
    private int startIteration = 10;
    private int graphWriteInterval = 25;
    private double pressureCost = 1.0;

    public PeerPressureAnalysisConfigGroup() {
        super(GROUP_NAME);
    }

    ///// Mode types public methods /////
    public String[] getModeTypes() {
        return getModeTypesString().split(",");
    }

    public void setModeTypes(String[] modeTypes) {
        setModeTypesString(Joiner.on(',').join(modeTypes));
    }

    @StringGetter("modeTypes")
    private String getModeTypesString() {
        return Joiner.on(',').join(this.modeTypes);
    }

    /// Mode types private methods ///
    @StringSetter("modeTypes")
    private void setModeTypesString(String modeTypes) {
        if (modeTypes == null) {
            this.modeTypes = null;
            return;
        }
        final String[] split = modeTypes.split(",");
        if (split.length != 2) throw new IllegalArgumentException(modeTypes);
        this.modeTypes = split;
    }

    //// \~~~~~~~~~

    // Iteration at which to start pressure
    @StringGetter("startIteration")
    public int getStartIterationField() {
        return this.startIteration;
    }

    @StringSetter("startIteration")
    public int setStartIterationField(int startIteration) {
        final int old = this.startIteration;
        this.startIteration = startIteration;
        return old;
    }

    @StringGetter("graphWriteInterval")
    public int getGraphWriteInterval() {
        return this.graphWriteInterval;
    }

    @StringSetter("graphWriteInterval")
    public void setGraphWriteInterval(final int i) {
        this.graphWriteInterval = i;
    }

    @StringGetter("pressureCost")
    public double getPressureCost() {
        return this.pressureCost;
    }

    @StringSetter("pressureCost")
    public void setPressureCost(final double i) {
        this.pressureCost = i;
    }

}
