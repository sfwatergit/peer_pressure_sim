package sandbox.sfwatergit.peerinfluence.io;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.io.GraphIOException;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetworkImpl;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.jung.factories.SocialVertexFactory;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson.SocialPerson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

import static sandbox.sfwatergit.peerinfluence.io.TextReaderTokens.TIE_SEP;
import static sandbox.sfwatergit.peerinfluence.io.TextReaderTokens.VE_SEP;

/**
 * Purpose of Class:
 * - Reads in graph as separated adjacency list file:
 * <p>
 * N;$numVertices
 * 1;2:0.1,3:0.1,4:0.8;0.5
 * 2;4:0.4,3:0.3,1:0.9;0.3
 * ...
 * $numVertices;2...
 * <p>
 * - Returns adjacency as ArrayList of ArrayLists of Longs for further processing
 * <p>
 */
public class SNTextReader implements SNReader {

    public static final String TYPE = "txt";

    private static Charset charset = Charset.forName("UTF-8");
    private SocialVertexFactory socialVertexFactory;
    long K;
    private Scenario sc;
    private String filename;
    private SocialNetwork socialNetwork = null;


    @Inject
    public SNTextReader(Scenario sc) {
        this.sc = sc;
        this.socialVertexFactory = person -> new SocialVertex(new SocialPerson(person.getId()));
    }


    @Override
    public void read(String filename) {
        this.filename = filename;

        try {
            socialNetwork = this.readGraph();
            if (socialNetwork.isEmpty()) {
                throw new GraphIOException("No data in graph");
            }
        } catch (GraphIOException e) {
            e.printStackTrace();
        }
        sc.addScenarioElement(SocialNetwork.ELEMENT_NAME, socialNetwork);

    }


    private Map<Id<Person>, Double> parseNeighborList(String[] neighbors) {
        Map<Id<Person>, Double> res = Maps.newHashMap();

        for (String neighborString : neighbors) {
            String[] split = neighborString.split(TIE_SEP);
            Id<Person> neighbor = Id.createPersonId(split[0]);
            res.put(neighbor, Double.valueOf(split[1]));
        }

        return res;
    }


    private SocialNetwork readGraph() throws GraphIOException {
        socialNetwork = new SocialNetworkImpl(new SparseMultigraph<>());
        final Map<Id<Person>, ? extends Person> pop = sc.getPopulation().getPersons();

        try {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), charset));

            String line;
            String header = in.readLine();
            String[] split = header.split(";");
            K = Long.parseLong(split[0]);

            for (int i = 1; i <= K; i++) {
                line = in.readLine();
                String[] data = line.split(VE_SEP);
                final Id<Person> egoId = Id.createPersonId(Long.parseLong(data[0].split(":")[0]));

                if (sc.getPopulation().getPersons().containsKey(egoId)) {

                    final SocialVertex ego = socialVertexFactory.create(new SocialPerson(egoId));
                    final Map<Id<Person>, Double> neighborMap = parseNeighborList(data);

                    neighborMap.keySet().stream()
                            .filter(pop::containsKey)
                            .map(id -> socialVertexFactory.create(new SocialPerson(id)))
                            .forEach(sv -> {
                                        if (!socialNetwork.containsVertex(sv)) socialNetwork.addSocialVertex(sv);
                                        socialNetwork.addRelationship(sv, ego, neighborMap.get(sv.getId()));
                                    }
                            );
                }


            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }

        return socialNetwork;
    }






}
