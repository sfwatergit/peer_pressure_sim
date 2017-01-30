package sandbox.sfwatergit.analysis.charts;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Various functions to take advantage of jung infrastructure for visualizing graphs.
 * <p>
 * Created by sidneyfeygin on 5/31/15.
 */
public class GraphVisualizer extends JApplet {


    final Color[] similarColors =
            {
                    new Color(216, 134, 134),
                    new Color(135, 137, 211),
                    new Color(134, 206, 189),
                    new Color(206, 176, 134),
                    new Color(194, 204, 134),
                    new Color(145, 214, 134),
                    new Color(133, 178, 209),
                    new Color(103, 148, 255),
                    new Color(60, 220, 220),
                    new Color(30, 250, 100)
            };
    VisualizationViewer<SocialVertex, SocialEdge> vv;
    @SuppressWarnings("unchecked")
    Map<SocialVertex, Paint> vertexPaints =
            LazyMap.decorate(new HashMap<>(),
                    new ConstantTransformer(Color.white));
    @SuppressWarnings("unchecked")
    Map<SocialEdge, Paint> edgePaints =
            LazyMap.decorate(new HashMap<>(),
                    new ConstantTransformer(Color.blue));

    private void colorCluster(Set<SocialVertex> vertices, Color c) {
        for (SocialVertex v : vertices) {
            vertexPaints.put(v, c);
        }
    }

    public void visualizeGraph(Graph<SocialVertex, SocialEdge> sG) {


        //Create a simple layout frame
        //specify the Fruchterman-Rheingold layout algorithm
        final AggregateLayout<SocialVertex, SocialEdge> layout =
                new AggregateLayout<>(new ISOMLayout<>(sG));

        vv = new VisualizationViewer<>(layout);
        vv.setBackground(Color.white);
        //Tell the renderer to use our own customized color rendering
        vv.getRenderContext().setVertexFillPaintTransformer(MapTransformer.getInstance(vertexPaints));
        vv.getRenderContext().setVertexDrawPaintTransformer(v -> {
            if (vv.getPickedVertexState().isPicked(v)) {
                return Color.cyan;
            } else {
                return Color.BLACK;
            }
        });
        vv.getRenderContext().setEdgeDrawPaintTransformer(MapTransformer.getInstance(edgePaints));

        vv.getRenderContext().setEdgeStrokeTransformer(new Transformer<SocialEdge, Stroke>() {
            protected final Stroke THIN = new BasicStroke(1);
            protected final Stroke THICK = new BasicStroke(2);

            public Stroke transform(SocialEdge e) {
                Paint c = edgePaints.get(e);
                if (c == Color.LIGHT_GRAY)
                    return THIN;
                else
                    return THICK;
            }
        });

        JButton scramble = new JButton("Restart");
        scramble.addActionListener(arg0 -> {
            Layout layout1 = vv.getGraphLayout();
            layout1.initialize();
            Relaxer relaxer = vv.getModel().getRelaxer();
            if (relaxer != null) {
                relaxer.stop();
                relaxer.prerelax();
                relaxer.relax();
            }
        });
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        vv.setGraphMouse(gm);
        final JToggleButton groupVertices = new JToggleButton("Group Clusters");
//Create slider to adjust the number of edges to remove when clustering
        final JSlider edgeBetweennessSlider = new JSlider(JSlider.HORIZONTAL);
        edgeBetweennessSlider.setBackground(Color.WHITE);
        edgeBetweennessSlider.setPreferredSize(new Dimension(210, 50));
        edgeBetweennessSlider.setPaintTicks(true);
        edgeBetweennessSlider.setMaximum(sG.getEdgeCount());
        edgeBetweennessSlider.setMinimum(0);
        edgeBetweennessSlider.setValue(0);
        edgeBetweennessSlider.setMajorTickSpacing(10);
        edgeBetweennessSlider.setPaintLabels(true);
        edgeBetweennessSlider.setPaintTicks(true);
        final JPanel eastControls = new JPanel();
        eastControls.setOpaque(true);
        eastControls.setLayout(new BoxLayout(eastControls, BoxLayout.Y_AXIS));
        eastControls.add(Box.createVerticalGlue());
        eastControls.add(edgeBetweennessSlider);

        final String COMMANDSTRING = "Edges removed for clusters: ";
        final String eastSize = COMMANDSTRING + edgeBetweennessSlider.getValue();

        final TitledBorder sliderBorder = BorderFactory.createTitledBorder(eastSize);
        eastControls.setBorder(sliderBorder);
        //eastControls.add(eastSize);
        eastControls.add(Box.createVerticalGlue());

        groupVertices.addItemListener(e -> {
            clusterAndRecolor(layout, edgeBetweennessSlider.getValue(),
                    similarColors, e.getStateChange() == ItemEvent.SELECTED);
            vv.repaint();
        });
        clusterAndRecolor(layout, 0, similarColors, groupVertices.isSelected());
        edgeBetweennessSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int numEdgesToRemove = source.getValue();
                clusterAndRecolor(layout, numEdgesToRemove, similarColors,
                        groupVertices.isSelected());
                sliderBorder.setTitle(
                        COMMANDSTRING + edgeBetweennessSlider.getValue());
                eastControls.repaint();
                vv.validate();
                vv.repaint();
            }
        });


        Container content = getContentPane();
        content.add(new GraphZoomScrollPane(vv));
        JPanel south = new JPanel();
        JPanel grid = new JPanel(new GridLayout(2, 1));
        grid.add(scramble);
        grid.add(groupVertices);
        south.add(grid);
        south.add(eastControls);
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        p.add(gm.getModeComboBox());
        south.add(p);
        content.add(south, BorderLayout.SOUTH);
    }

    public void clusterAndRecolor(AggregateLayout<SocialVertex, SocialEdge> layout,
                                  int numEdgesToRemove,
                                  Color[] colors, boolean groupClusters) {
        //Now cluster the vertices by removing the top 50 edges with highest betweenness
        //		if (numEdgesToRemove == 0) {
        //			colorCluster( g.getVertices(), colors[0] );
        //		} else {

        Graph<SocialVertex, SocialEdge> g = layout.getGraph();
        layout.removeAll();

        EdgeBetweennessClusterer<SocialVertex, SocialEdge> clusterer =
                new EdgeBetweennessClusterer<>(numEdgesToRemove);

        Set<Set<SocialVertex>> clusterSet = clusterer.transform(g);
        java.util.List<SocialEdge> edges = clusterer.getEdgesRemoved();

        int i = 0;
        //Set the colors of each node so that each cluster's vertices have the same color
        for (Set<SocialVertex> vertices : clusterSet) {

            Color c = colors[i % colors.length];

            colorCluster(vertices, c);
            if (groupClusters) {
                groupCluster(layout, vertices);
            }
            i++;
        }
        for (SocialEdge e : g.getEdges()) {

            if (edges.contains(e)) {
                edgePaints.put(e, Color.lightGray);
            } else {
                edgePaints.put(e, Color.black);
            }
        }
    }

    private void groupCluster(AggregateLayout<SocialVertex, SocialEdge> layout, Set<SocialVertex> vertices) {
        if (vertices.size() < layout.getGraph().getVertexCount()) {
            Point2D center = layout.transform(vertices.iterator().next());
            Graph<SocialVertex, SocialEdge> subGraph = SparseMultigraph.<SocialVertex, SocialEdge>getFactory().create();
            vertices.forEach(subGraph::addVertex);
            Layout<SocialVertex, SocialEdge> subLayout =
                    new CircleLayout<>(subGraph);
            subLayout.setInitializer(vv.getGraphLayout());
            subLayout.setSize(new Dimension(40, 40));
            layout.put(subLayout, center);
            vv.repaint();
        }
    }


}
