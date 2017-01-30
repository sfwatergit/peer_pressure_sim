//package sandbox.sfwatergit.analysis.gis;
//
//import com.google.common.collect.Lists;
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.Point;
//import org.geotools.data.DataUtilities;
//import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.data.simple.SimpleFeatureIterator;
//import org.geotools.data.simple.SimpleFeatureSource;
//import org.geotools.factory.CommonFactoryFinder;
//import org.matsim.core.utils.geometry.geotools.MGC;
//import org.matsim.core.utils.gis.PolylineFeatureFactory;
//import org.matsim.core.utils.gis.ShapeFileReader;
//import org.matsim.core.utils.gis.ShapeFileWriter;
//import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.filter.Filter;
//import org.opengis.filter.FilterFactory;
//
//import java.io.IOException;
//import java.util.List;
//
///**
// * Connects points in two point shapefile using polylines.
// * <p>
// * Currently being used to connect home to work locations.
// * <p>
// * Created by sidneyfeygin on 11/14/15.
// */
//public class ShpPointConnector {
//    private final SimpleFeatureSource sourceSource;
//    private final SimpleFeatureSource targetSource;
//    private List<SimpleFeature> polyLineFeatures;
//
//
//    public ShpPointConnector(String sourceFile, String targetFile) {
//        ShapeFileReader shapeFileReader = new ShapeFileReader();
//
//        // Get source
//        shapeFileReader.readFileAndInitialize(sourceFile);
//        List<SimpleFeature> sourcePointLayer = (List<SimpleFeature>) shapeFileReader.getFeatureSet();
//
//        // Get target
//        shapeFileReader.readFileAndInitialize(targetFile);
//        List<SimpleFeature> targetPointLayer = (List<SimpleFeature>) shapeFileReader.getFeatureSet();
//
//        // Read SimpleFeature sets into FeatureCollections for efficient querying
//        SimpleFeatureCollection sourceCollection = DataUtilities.collection(sourcePointLayer);
//        sourceSource = DataUtilities.source(sourceCollection);
//        SimpleFeatureCollection targetCollection = DataUtilities.collection(targetPointLayer);
//        targetSource = DataUtilities.source(targetCollection);
//
//        // initialize the line feature layer container
//        polyLineFeatures = Lists.newArrayList();
//
//    }
//
//    // main method for testing
//    public static void main(String[] args) {
//        final ShpPointConnector myShpConnector = new ShpPointConnector("/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources/output_archives/sf_bay/bart_caltrain/travelTime/shp/car_to_pt.shp", "/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources/output_archives/sf_bay/bart_caltrain/PtAccessibility/activityLocations_w1.shp");
//
//        try {
//            myShpConnector.createLines("epsg:32610", "personId", "car_to_pt_to_w1", "/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources/output_archives/sf_bay/bart_caltrain/travelTime/shp/");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void createLines(String crs, String joinAttribute, String outputName, String outDir) throws IOException {
//
//        // Initialize line layer
//        PolylineFeatureFactory.Builder lffb = new PolylineFeatureFactory.Builder();
//        final PolylineFeatureFactory polylineFeatureFactory = lffb.addAttribute(joinAttribute, String.class).setName(outputName).setCrs(MGC.getCRS(crs)).create();
//
//        // filter by id in sources
//        final FilterFactory ff = CommonFactoryFinder.getFilterFactory();
//        final SimpleFeatureCollection features = sourceSource.getFeatures();
//
//        // iterate over points
//        try (SimpleFeatureIterator i = features.features()) {
//            while (i.hasNext()) {
//
//                // initialize the line coordinate array
//                Coordinate[] lineCoordinateArray = new Coordinate[2];
//                SimpleFeature sourcePoint = i.next();
//                final Point sourcePointGeometry = (Point) sourcePoint.getDefaultGeometry();
//                final Coordinate sourceCoordinate = sourcePointGeometry.getCoordinate();
//                lineCoordinateArray[0] = sourceCoordinate;
//                Long longId = (Long) sourcePoint.getAttribute("id");
//                Filter equalFilter = ff.equals(ff.property("personId"), ff.literal(String.valueOf(longId)));
//                SimpleFeatureCollection targetPointCollection = targetSource.getFeatures(equalFilter);
//                SimpleFeature targetPoint = null;
//
//                // filter target
//                try (SimpleFeatureIterator j = targetPointCollection.features()) {
//                    if (j.hasNext()) {
//                        targetPoint = j.next();
//                    }
//                }
//
//                // found the target point, now create the line
//                if (targetPoint != null) {
//                    Point targetPointGeometry = (Point) targetPoint.getDefaultGeometry();
//                    final Coordinate targetCoordinate = targetPointGeometry.getCoordinate();
//                    lineCoordinateArray[1] = targetCoordinate;
//                    final SimpleFeature polyline = polylineFeatureFactory.createPolyline(lineCoordinateArray);
//                    polyLineFeatures.add(polyline);
//                } else {
//
//                    //TODO: improved exception handling
//                    throw new RuntimeException("id not found");
//                }
//            }
//        }
//
//        // Write out the final file
//        ShapeFileWriter.writeGeometries(polyLineFeatures, outDir + outputName + ".shp");
//    }
//
//}
