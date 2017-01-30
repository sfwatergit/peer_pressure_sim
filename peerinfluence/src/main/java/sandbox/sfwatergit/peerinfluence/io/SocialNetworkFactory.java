package sandbox.sfwatergit.peerinfluence.io;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Scenario;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.run.config.SocialNetworkConfigGroup;

/**
 * Created by sidneyfeygin on 1/23/16.
 */
@Singleton
public class SocialNetworkFactory implements Provider<SocialNetwork> {

    private final SNReader snReader;
    private final String snfilename;
    private final Scenario scenario;

    @Inject
    public SocialNetworkFactory(SNReader snReader, @Named(SocialNetworkConfigGroup.SN_FILE_ELEMENT_NAME)
            String snfilename, Scenario scenario) {
        this.snReader = snReader;
        this.snfilename = snfilename;
        this.scenario = scenario;
    }

    @Override
    public SocialNetwork get() {
        snReader.read(snfilename);
        return (SocialNetwork) scenario.getScenarioElement(SocialNetwork.ELEMENT_NAME);
    }
}
