package sandbox.sfwatergit.utils.pt.gtfsUtils.debugging;

import com.google.common.collect.Sets;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeAndDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.pt.*;

import java.util.List;
import java.util.Set;

/**
 * Temporary instrumented UmlaufInterpolator to detect bad stops w/out throwing RunTime exceptions.
 *
 * @author sfeygin
 */
public class MyUmlaufInterpolator extends UmlaufInterpolator {

	private final Network network;
    private final LeastCostPathCalculator routingAlgo;
	private final Set<String> badNodeIds;
    private final Set<Node> badNodes=Sets.newHashSet();

	public MyUmlaufInterpolator(Network network, final PlanCalcScoreConfigGroup config) {
		super(network,config);
		this.network = network;
        FreespeedTravelTimeAndDisutility travelTimes = new FreespeedTravelTimeAndDisutility(config);
		this.routingAlgo = new Dijkstra(network, travelTimes, travelTimes);
		badNodeIds = Sets.newHashSet();
	}

	public void addUmlaufStueckToUmlauf(UmlaufStueck umlaufStueck, Umlauf umlauf) {
		List<UmlaufStueckI> umlaufStueckeOfThisUmlauf = umlauf.getUmlaufStuecke();
		if (! umlaufStueckeOfThisUmlauf.isEmpty()) {
			UmlaufStueckI previousUmlaufStueck = umlaufStueckeOfThisUmlauf.get(umlaufStueckeOfThisUmlauf.size() - 1);
			NetworkRoute previousCarRoute = previousUmlaufStueck.getCarRoute();
			Id<Link> fromLinkId = previousCarRoute.getEndLinkId();
			Id<Link> toLinkId = umlaufStueck.getCarRoute().getStartLinkId();
			if (!fromLinkId.equals(toLinkId)) {
				insertWenden(fromLinkId, toLinkId, umlauf);
			}
		}
		umlaufStueckeOfThisUmlauf.add(umlaufStueck);
	}

	private void insertWenden(Id<Link> fromLinkId, Id<Link> toLinkId, Umlauf umlauf) {
		Node startNode = this.network.getLinks().get(fromLinkId).getToNode();
		Node endNode = this.network.getLinks().get(toLinkId).getFromNode();
		double depTime = 0.0;
        if(badNodes.contains(startNode) || badNodes.contains(endNode)){
            return;
        }
		Path wendenPath = routingAlgo.calcLeastCostPath(startNode, endNode, depTime, null, null);

		if (wendenPath == null) {
			final Id<Node> badIdF = startNode.getId();
            final Id<Node> badIdT = endNode.getId();
            final String sF = badIdF.toString();
			final String sT = badIdT.toString();

			if(sF.contains("node")) {
				badNodeIds.add(sF);
                badNodes.add(startNode);
            }
			if (sT.contains("node")){
                badNodeIds.add(sT);
                badNodes.add(endNode);
            }
//			throw new RuntimeException("No route found from node "
//					+ startNode.getId() + " to node " + endNode.getId() + ".");
		}else {
			NetworkRoute route = new LinkNetworkRouteImpl(fromLinkId, toLinkId);
			route.setLinkIds(fromLinkId, NetworkUtils.getLinkIds(wendenPath.links), toLinkId);
			umlauf.getUmlaufStuecke().add(new Wenden(route));
		}
	}

	public Set<String> getBadNodeIds() {
		return badNodeIds;
	}
}
