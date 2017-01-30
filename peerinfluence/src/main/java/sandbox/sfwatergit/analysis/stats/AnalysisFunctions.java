package sandbox.sfwatergit.analysis.stats;

import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.Graph;
import hep.aida.bin.StaticBin1D;
import org.apache.commons.collections15.Transformer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialEdge;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;

import java.io.FileOutputStream;
import java.util.Map;

/**
 * Created by sidneyfeygin on 1/16/16.
 */
public class AnalysisFunctions {
    public static void writeAnalysis(String filename,Graph<SocialVertex,SocialEdge> g){
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Graph Analysis Data");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(1).setCellValue("Number of Agents");
        headerRow.createCell(2).setCellValue("Average Degree");
        headerRow.createCell(3).setCellValue("Minimum Degree");
        headerRow.createCell(4).setCellValue("Maximum Degree");
        headerRow.createCell(5).setCellValue("Diameter");
//        headerRow.createCell(6).setCellValue("Average Distance");
//        headerRow.createCell(7).setCellValue("Average Clustering Coefficient");

        int currentNumberOfAgents = g.getVertexCount();

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(1).setCellValue("N=" + currentNumberOfAgents);
        dataRow.createCell(2).setCellValue(averageDegree.analyze(g));
        dataRow.createCell(3).setCellValue(minimumDegree.analyze(g));
        dataRow.createCell(4).setCellValue(maximumDegree.analyze(g));
        dataRow.createCell(5).setCellValue(diameter.analyze(g));
//        dataRow.createCell(6).setCellValue(averageDistance.analyze(g));
//        dataRow.createCell(7).setCellValue(averageClusteringCoefficient.analyze(g));



        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filename);
            workbook.write(outputStream);
            outputStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }


    public static AnalysisTask<Integer> averageDegree = (g) -> (2 * g.getEdgeCount()) / g.getVertexCount();

    public static AnalysisTask<Double> maximumDegree = (g) -> getDegrees(g).max();

    public static AnalysisTask<Double> minimumDegree = (g) -> getDegrees(g).min();

    public static AnalysisTask<Double> diameter = DistanceStatistics::diameter;

    public static AnalysisTask<Double> averageDistance = (g)->{
        final Transformer<SocialVertex, Double> distanceMaps = DistanceStatistics.averageDistances(g);
        StaticBin1D averageDistances = new StaticBin1D();
        for(SocialVertex sv : g.getVertices()) {
            averageDistances.add(1/distanceMaps.transform(sv));
        }
        return averageDistances.mean();
    };

    public static AnalysisTask<Double> averageClusteringCoefficient = (g)->{
        final Map<SocialVertex, Double> svMap = Metrics.clusteringCoefficients(g);
        StaticBin1D clusteringCoefficients = new StaticBin1D();

        for(SocialVertex sv : g.getVertices()) {
            clusteringCoefficients.add(svMap.get(sv));
        }

        return clusteringCoefficients.mean();
    };


    private static StaticBin1D getDegrees(Graph<SocialVertex, SocialEdge> graph) {
        StaticBin1D degrees = new StaticBin1D();

        for(SocialVertex vertex : graph.getVertices()) {
            degrees.add(graph.degree(vertex));
        }

        return degrees;
    }






}
