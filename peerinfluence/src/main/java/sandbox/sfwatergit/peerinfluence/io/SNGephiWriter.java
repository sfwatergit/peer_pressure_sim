package sandbox.sfwatergit.peerinfluence.io;

import it.uniroma1.dis.wsngroup.gexf4j.core.*;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Write modeshifts to gephi file for further travelTime
 * <p>
 * Created by sidneyfeygin on 2/3/16.
 */
public class SNGephiWriter  {
    private static final Logger log = Logger.getLogger(SNGephiWriter.class);
    private final Gexf gexf;
    private static Map<Id<Person>,Node> id2NodeMap = new HashMap<>();


    public SNGephiWriter() {
        gexf = new GexfImpl();
    }

    public static void main(String[] args) {
        String graphFile = args[0];
        final Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.loadConfig(args[1]));
        SNTextReader snTextReader = new SNTextReader(scenario);
        snTextReader.read(graphFile);
        SocialNetwork socialNetwork = (SocialNetwork) scenario.getScenarioElement(SocialNetwork.ELEMENT_NAME);
        try {
            SNPajekWriter.writeGraph(socialNetwork, IOUtils.getOutputStream("sf_social.net"));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        SNGephiWriter sn = new SNGephiWriter();
//        sn.writeGraph(socialNetwork);
    }


    public void writeGraph(SocialNetwork socialNetwork) {

        Graph graph = gexf.getGraph();
        graph.setDefaultEdgeType(EdgeType.UNDIRECTED).setMode(Mode.STATIC);

        socialNetwork.getSocialVertices().forEach((id,sv)->{
            Node n = graph.createNode(id.toString());
            n.setLabel(id.toString());
            id2NodeMap.put(id,n);
        });

        Integer i = 0;

        for (SocialEdge se : socialNetwork.getEdges()) {
            final SocialVertex alter = se.getAlter();
            final SocialVertex ego = se.getEgo();
            final Node node = id2NodeMap.get(alter.getId());
            node.connectTo(i.toString(),id2NodeMap.get(ego.getId()));
            i++;
        }



        StaxGraphWriter graphWriter = new StaxGraphWriter();

        File f = new File("sf_social.gexf");
        FileOutputStream fileOut;
        try {
            log.info("Writing pressure social network to file");
            log.info(f.getAbsolutePath());
            fileOut = new FileOutputStream(f);
            graphWriter.writeToStream(gexf, fileOut, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
