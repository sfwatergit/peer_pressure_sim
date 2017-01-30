package sandbox.sfwatergit.peerinfluence.run.modules;

import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.AbstractModule;
import sandbox.sfwatergit.peerinfluence.internalization.externalitytracking.ExternalityManager;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.FlaggedPlanModifier;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.PressureDataManager;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.routing.SwitchFlaggedModeStrategyFactory;
import sandbox.sfwatergit.peerinfluence.run.config.PeerPressureAnalysisConfigGroup;

/**
 * Created by sidneyfeygin on 1/7/16.
 */
public class PeerPressureModule extends AbstractModule {


    /**
     * The weight of the switched flag mode strategy
     *
     * TODO: make configurable from config file and remove this field
     */
    private static final double STRATEGY_WEIGHT = 1d;

    @Override
    public void install() {

        Config config = getConfig();

        // Plan strategy
        addPlanStrategyBinding(SwitchFlaggedModeStrategyFactory.STRATEGY_NAME).toProvider(SwitchFlaggedModeStrategyFactory.class);

        // Strategy settings
        StrategyConfigGroup.StrategySettings stratSets = new StrategyConfigGroup.StrategySettings();
        stratSets.setStrategyName(SwitchFlaggedModeStrategyFactory.STRATEGY_NAME);
        stratSets.setWeight(STRATEGY_WEIGHT);
        config.strategy().addStrategySettings(stratSets);
        addControlerListenerBinding().to(FlaggedPlanModifier.class);

        // Start iteration
        binder().bindConstant().annotatedWith(Names.named(PeerPressureAnalysisConfigGroup.START_PRESSURE_ITERATION)).to(config.getParam(PeerPressureAnalysisConfigGroup.GROUP_NAME, PeerPressureAnalysisConfigGroup.START_PRESSURE_ITERATION));

        // Pressure Cost
        binder().bindConstant().annotatedWith(Names.named(PeerPressureAnalysisConfigGroup.PRESSURE_COST)).to(config.getModule(PeerPressureAnalysisConfigGroup.GROUP_NAME).getValue(PeerPressureAnalysisConfigGroup.PRESSURE_COST));

        bind(ExternalityManager.class).in(Singleton.class);
        bind(PressureDataManager.class);
        addControlerListenerBinding().to(PressureDataManager.class);


    }
}
