package sandbox.sfwatergit.peerinfluence.run.config;


import org.matsim.core.config.ReflectiveConfigGroup;

/**
 * Created by sidneyfeygin on 7/28/15.
 */
public class ExternalitiesConfigGroup extends ReflectiveConfigGroup {

    public static final String GROUP_NAME = "externalitiesInternalization";
    public static final String EMISSIONS_COST_RATIO = "emissionsCostRatio";
    public static final String CONGESTION_COST_FACTOR = "congestionCostRatio";

    // Private fields and default values
    private double congestionCostRatio = Double.NaN;
    private double emissionsCostRatio = Double.NaN;

    public ExternalitiesConfigGroup() {
        super(GROUP_NAME);
    }

    // Internalization emissions cost factor
    @StringGetter("emissionsCostRatio")
    public double getEmissionsCostRatioField() {
        return this.emissionsCostRatio;
    }

    @StringSetter("emissionsCostRatio")
    public double setEmissionsCostFactorField(float emissionsCost) {
        final double old = this.emissionsCostRatio;
        this.emissionsCostRatio = emissionsCost;
        return old;
    }

    @StringGetter("congestionCostRatio")
    public double getCongestionCostRatioField() {
        return this.congestionCostRatio;
    }

    @StringSetter("congestionCostRatio")
    public void setCongestionCostRatioField(double congestionCostRatio) {
        final double old = this.congestionCostRatio;
        this.congestionCostRatio = congestionCostRatio;

    }

}
