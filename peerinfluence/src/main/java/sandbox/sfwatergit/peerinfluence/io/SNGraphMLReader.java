package sandbox.sfwatergit.peerinfluence.io;

import com.google.common.collect.Lists;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.io.GraphMLReader;
import org.apache.commons.collections15.Factory;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.xml.sax.SAXException;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.jung.socialpeople.RandomSocialPerson;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson.SocialPerson;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by sidneyfeygin on 8/11/15.
 */
public class SNGraphMLReader implements SNReader{
    public static final String TYPE = "graphml";
    Random random = new Random(123);
    List<Integer> verticies = Lists.newArrayList();



    public static void main(String[] args) {
        SNReader reader = new SNGraphMLReader();
        reader.read("/Users/sidneyfeygin/current_code/java/matsim_home/matsim_smartcities/src/main/resources/test.graphml");


    }

    @Override
    public void read(String filename) {

        Factory<SocialVertex> vertexFactory = new Factory<SocialVertex>() {
            Integer n = 0;

            @Override
            public SocialVertex create() {
                n++;
                verticies.add(n);
                return new SocialVertex(new SocialPerson((Id.createPersonId(n))));
            }
        };

        Factory<SocialEdge> edgeFactory = new Factory<SocialEdge>() {

            public SocialEdge check() {
                List<SocialEdge> cont = Lists.newArrayList();
                final Integer i = random.nextInt(verticies.size());
                final Id<Person> personId =Id.createPersonId(i);
                final SocialVertex first = new SocialVertex(new SocialPerson(personId));


                SocialEdge r = new SocialEdge(first, new SocialVertex(new RandomSocialPerson(new Random(12345))));

                if (!cont.contains(r)) {
                    cont.add(r);
                    return r;
                } else {
                    return check();
                }
            }


            public SocialEdge create() {
                return check();
            }
        };

        GraphMLReader<SparseGraph<SocialVertex, SocialEdge>, SocialVertex, SocialEdge> gmlr = null;
        try {
            gmlr = new GraphMLReader<>(vertexFactory, edgeFactory);
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        final SparseGraph<SocialVertex, SocialEdge> graph = new SparseGraph<>();
        try {
            if (gmlr != null) {
                gmlr.load(filename, graph);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
