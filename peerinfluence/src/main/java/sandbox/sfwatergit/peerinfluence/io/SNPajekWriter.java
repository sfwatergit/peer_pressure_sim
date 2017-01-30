package sandbox.sfwatergit.peerinfluence.io;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.io.PajekNetWriter;
import org.apache.commons.collections15.Transformer;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by sidneyfeygin on 5/27/15.
 */
public class SNPajekWriter {
    public static void writeGraph(SocialNetwork graph, OutputStream stream) throws IOException {
        
        PajekNetWriter<SocialVertex, SocialEdge> writer = new PajekNetWriter<>();
        
        OutputStreamWriter os = new OutputStreamWriter(stream);
        
        Transformer<SocialVertex, String> v = SocialVertex::toString;
        
        Transformer<SocialEdge, Number> nev = SocialEdge::getTieStrength;

        writer.save((Graph<SocialVertex, SocialEdge>) graph.getGraph(), os, v, nev);
    }

}
