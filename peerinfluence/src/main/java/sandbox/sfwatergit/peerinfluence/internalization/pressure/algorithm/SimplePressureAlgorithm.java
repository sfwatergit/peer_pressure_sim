package sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm;

import com.google.inject.Inject;
import sandbox.sfwatergit.peerinfluence.internalization.InternalizationListener;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.util.EventListener;
import java.util.Set;

/**
 * Used to pressureEgo the subgame equilibrium pressure of the first round.
 * <p>
 * <p>
 * <p>
 * TODO: Modularize and decouple from {@link InternalizationListener}
 * Created by sidneyfeygin on 7/10/15.
 */
public class SimplePressureAlgorithm implements EventListener, PressureAlgorithm {

    private final SocialNetwork socialNetwork; //Defined as singleton object in module, so injected scope should be valid here

    @Inject
    public SimplePressureAlgorithm(SocialNetwork socialNetwork) {

        this.socialNetwork = socialNetwork;
    }



    ////// Getters/Setters ////////

    /**
     * In the first round, peers choose amount of pressure to exert with
     * goal of maximizing personal utility.
     * <br/>
     * <ul><li>Algorithm works as follows :
     * <p>
     * <li>Iterate over neighbors and choose peers that pressure ego</li>
     * <p>
     * <li>How to choose peers:
     * <p>
     * <li>Peers that pressure either use public transit or have greater driving utility than ego</li>
     * </li>
     * <p>
     * <li>Pressure deficit to ego is assigned as the sum of peers' scores.</li>
     * <p>
     * <li>SocialPersonPeaPod is flagged if pressure is in excess of ego utiilty difference times cost factor</li>
     * </li>
     * </ul>
     * @return amount of pressure on ego or 0.0 if none
     */
    @Override
    public Double computeEgoPressure(Set<SocialVertex> rels, PressureFunction pressureFunction) {
        return rels.stream().mapToDouble(pressureFunction).filter(Double::isFinite).sum();
    }


//    public void matrixOp() {
//        final SparseDoubleMatrix2D socioMatrix = socialNetwork.getAsSparseMatrix();
//        final Collection<SocialVertex> vertices = socialNetwork.getVertices();
//        BidiMap<SocialVertex, Integer> indexer = Indexer.create(vertices);
//        for (SocialVertex vertex : vertices) {
//            final Integer p_i = indexer.get(vertex);
//            final DoubleMatrix1D row = socioMatrix.viewRow(p_i);
//
//        }
//
//    }


}
