package sandbox.sfwatergit.peerinfluence.run.modules;

import com.google.common.io.Files;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.apache.log4j.Logger;
import org.matsim.core.controler.AbstractModule;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.io.*;
import sandbox.sfwatergit.peerinfluence.run.config.PeerPressureAnalysisConfigGroup;
import sandbox.sfwatergit.peerinfluence.run.config.SocialNetworkConfigGroup;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by sidneyfeygin on 6/8/15.
 */
public class SocialNetworkModule extends AbstractModule {


    public final Logger log = Logger.getLogger(SocialNetworkModule.class);


    public SocialNetworkModule() {

    }

    @Override
    public void install() {

        final String socialNetFilename = getConfig().getModule(SocialNetworkConfigGroup.GROUP_NAME).getValue(SocialNetworkConfigGroup.SN_FILE_ELEMENT_NAME);
        binder().bindConstant().annotatedWith(Names.named(SocialNetworkConfigGroup.SN_FILE_ELEMENT_NAME)).to(socialNetFilename);

        // Start pressure iteration
        binder().bindConstant().annotatedWith(Names.named(PeerPressureAnalysisConfigGroup.START_PRESSURE_ITERATION)).to(getConfig().getModule(PeerPressureAnalysisConfigGroup.GROUP_NAME).getValue(PeerPressureAnalysisConfigGroup.START_PRESSURE_ITERATION));


        // Social person reader type binder
        final String fileExtension = checkNotNull(Files.getFileExtension(socialNetFilename));
        switch (fileExtension) {
            case SNXmlReader.TYPE: {
                bind(SNReader.class).to(SNXmlReader.class);
                break;
            }
            case SNTextReader.TYPE: {
                bind(SNReader.class).to(SNTextReader.class);
                break;
            }
            case SNPajekReader.TYPE: {
                bind(SNReader.class).to(SNPajekReader.class);
                break;
            }
            case SNGraphMLReader.TYPE: {
                bind(SNReader.class).to(SNGraphMLReader.class);
                break;
            }
            default:
                bind(SNReader.class).to(SNTextReader.class);
                break;
        }


        // Social network type binder
        bind(SocialNetwork.class).toProvider(SocialNetworkFactory.class).in(Singleton.class);  // singleton b/c scope is app scope




    }


}