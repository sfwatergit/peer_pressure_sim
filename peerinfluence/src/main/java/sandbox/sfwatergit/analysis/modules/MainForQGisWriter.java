//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package sandbox.sfwatergit.analysis.modules;

import org.matsim.contrib.analysis.vsp.qgis.QGisConstants.geometryType;
import org.matsim.contrib.analysis.vsp.qgis.QGisWriter;
import org.matsim.contrib.analysis.vsp.qgis.RasterLayer;
import org.matsim.contrib.analysis.vsp.qgis.VectorLayer;
import org.matsim.contrib.analysis.vsp.qgis.layerTemplates.AccessibilityDensitiesRenderer;
import org.matsim.contrib.analysis.vsp.qgis.layerTemplates.AccessibilityRenderer;
import org.matsim.contrib.analysis.vsp.qgis.layerTemplates.AccessibilityXmlRenderer;

public class MainForQGisWriter {
    public MainForQGisWriter() {
    }

    public static void main(String[] args) {
        String workingDirectory = "/Users/sidneyfeygin/current_code/java/matsim_home/matsim_smartcities/test/output/siouxfalls/";
        String qGisProjectFile = "testWithMergedImmissionsCSV.qgs";
        QGisWriter writer = new QGisWriter("WGS84_SA_Albers", workingDirectory);
        double[] extent = new double[]{2790381.0D, -4035858.0D, 2891991.0D, -3975105.0D};
        Double lowerBound = Double.valueOf(1.75D);
        Double upperBound = Double.valueOf(7.0D);
        Integer range = Integer.valueOf(9);
        short symbolSize = 1010;
        short cellSize = 1000;
        int populationThreshold = 200 / (1000 / cellSize * 1000 / cellSize);

        RasterLayer mapnikLayer = new RasterLayer("osm_mapnik_xml", workingDirectory + "testfiles/accessibility/osm_mapnik.xml");
        new AccessibilityXmlRenderer(mapnikLayer);
        mapnikLayer.setSrs("WGS84_Pseudo_Mercator");
        writer.addLayer(mapnikLayer);
        VectorLayer densityLayer = new VectorLayer("density", workingDirectory + "testFiles/accessibility/accessibilities.csv", geometryType.Point);
        densityLayer.setXField(1);
        densityLayer.setYField(2);
        AccessibilityDensitiesRenderer dRenderer = new AccessibilityDensitiesRenderer(densityLayer, populationThreshold, symbolSize);
        dRenderer.setRenderingAttribute(8);
        writer.addLayer(densityLayer);
        VectorLayer accessibilityLayer = new VectorLayer("accessibility", workingDirectory + "testFiles/accessibility/accessibilities.csv", geometryType.Point);
        accessibilityLayer.setXField(1);
        accessibilityLayer.setYField(2);
        AccessibilityRenderer renderer = new AccessibilityRenderer(accessibilityLayer, upperBound, lowerBound, range, symbolSize);
        renderer.setRenderingAttribute(3);
        writer.addLayer(accessibilityLayer);
        writer.write(qGisProjectFile);
    }
}
