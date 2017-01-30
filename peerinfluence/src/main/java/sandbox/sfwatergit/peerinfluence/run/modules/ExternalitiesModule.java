package sandbox.sfwatergit.peerinfluence.run.modules;

import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import sandbox.sfwatergit.peerinfluence.internalization.InternalizationListener;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGCostModule;
import sandbox.sfwatergit.peerinfluence.internalization.ghg.GHGModule;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm.SimplePressureAlgorithm;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.routing.GHGCongestionTravelDisutilityCalculatorFactory;
import sandbox.sfwatergit.peerinfluence.run.config.ExternalitiesConfigGroup;
import sandbox.sfwatergit.peerinfluence.run.config.PeerPressureAnalysisConfigGroup;

/**
 * Created by sidneyfeygin on 7/28/15.
 */
public class ExternalitiesModule extends AbstractModule {


    @Override
    public void install() {
        Config config = getConfig();

        // Congestion cost factor
        binder().bindConstant().annotatedWith(Names.named(ExternalitiesConfigGroup.CONGESTION_COST_FACTOR)).to(Double.parseDouble(config.getParam(ExternalitiesConfigGroup.GROUP_NAME, ExternalitiesConfigGroup.CONGESTION_COST_FACTOR)));

        // Emissions cost factor binding
        binder().bindConstant().annotatedWith(Names.named(ExternalitiesConfigGroup.EMISSIONS_COST_RATIO)).to(Double.parseDouble(config.getParam(ExternalitiesConfigGroup.GROUP_NAME, ExternalitiesConfigGroup.EMISSIONS_COST_RATIO)));

        // Greenhouse gas emissions module
        bind(GHGModule.class).in(Singleton.class);

        // Greenhouse gas cost module
        bind(GHGCostModule.class).in(Singleton.class);

        // Calculates disutility for travel on links
        bindCarTravelDisutilityFactory().to(GHGCongestionTravelDisutilityCalculatorFactory.class);


        // Mode types for pressure travelTime
        binder().bindConstant().annotatedWith(Names.named(PeerPressureAnalysisConfigGroup.MODE_TYPES)).to("pt,car");


        // Bind as singleton
        bind(InternalizationListener.class).in(Singleton.class);

        // Add event handler
        addEventHandlerBinding().to(InternalizationListener.class);

        // Internalizes GHG scores (main entry to algorithm)
        addControlerListenerBinding().to(InternalizationListener.class);


        // Pressure algorithm is in internalization listener
        bind(SimplePressureAlgorithm.class).in(Singleton.class);

    }


}
