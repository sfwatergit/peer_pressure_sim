package sandbox.sfwatergit.peerinfluence.io;

import com.google.inject.Inject;
import edu.uci.ics.jung.algorithms.util.SettableTransformer;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.io.PajekNetReader;
import org.apache.commons.collections15.Factory;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetworkImpl;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson.SocialPerson;

import java.io.IOException;
import java.util.Random;

/**
 * Read a graph from Pajek representation
 * Created by sidneyfeygin on 5/27/15.
 */
public class SNPajekReader implements SNReader {
    public static final String TYPE = "net";
    private final Scenario scenario;



    @Inject
    public SNPajekReader(Scenario scenario) {
        this.scenario = scenario;
    }



    @Override
    public void read(String filename) {
        SocialNetwork sn = null;
        // Needs to have random factory added due to lack of alternative identifier

        final PajekNetReader<SparseMultigraph<SocialVertex, SocialEdge>, SocialVertex, SocialEdge> pr = new PajekNetReader<>(
                new Factory<SocialVertex>() {
                    Random id = new Random();

                    @Override
                    public SocialVertex create() {
                        return new SocialVertex(
                                new SocialPerson(
                                        Id.createPersonId(id.nextLong())));
                    }
                }, new Factory<SocialEdge>() {
            Random id = new Random();

            @Override
            public SocialEdge create() {
                return new SocialEdge(String.valueOf(id.nextDouble()));
            }
        });


        pr.setEdgeWeightTransformer(new SettableTransformer<SocialEdge, Number>() {
            @Override
            public void set(SocialEdge socialEdge, Number o) {
                socialEdge.setTieStrength(Double.valueOf(String.valueOf(o)));
            }

            @Override
            public Number transform(SocialEdge socialEdge) {
                return socialEdge.getTieStrength();
            }
        });
        try {
            sn = new SocialNetworkImpl(pr.load(filename, SparseMultigraph::new));
        } catch (IOException e) {
            e.printStackTrace();
        }
        scenario.addScenarioElement(SocialNetwork.ELEMENT_NAME,sn);
    }
}
