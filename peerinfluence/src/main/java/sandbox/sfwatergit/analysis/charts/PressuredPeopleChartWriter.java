package sandbox.sfwatergit.analysis.charts;

import gnu.trove.TDoubleArrayList;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.util.ShapeUtilities;
import org.matsim.core.utils.charts.ChartUtil;
import sandbox.sfwatergit.peerinfluence.internalization.InternalizationListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by sidneyfeygin on 7/28/15.
 */
public class PressuredPeopleChartWriter extends ChartUtil {

    private static final Logger log = Logger.getLogger(InternalizationListener.class);
    protected DgAxisBuilder axisBuilder = new DgDefaultAxisBuilder();
    private TDoubleArrayList iterations;
    private TDoubleArrayList peoplePressured;
    private String xLabel = "Iteration";
    private String yLabel1 = "Mode Share (Percent)";
    private String yLabel2 = "People Pressured";
    private List<Map<String, Double>> iterationsModePercent;

    public PressuredPeopleChartWriter(String title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel);
        log.info("Creating pressure and mode share chart");
    }

    public void writeChart(TDoubleArrayList iterations, TDoubleArrayList peoplePressured, List<Map<String, Double>> iterationsModeCounts, String filename) {
        this.iterations = iterations;
        this.peoplePressured = peoplePressured;
        this.iterationsModePercent = iterationsModeCounts;
        this.chart = getChart();
        saveAsPng(filename, 1200, 800);
    }

    private List<String> getUsedModes() {
        List<String> modes = new ArrayList<String>();

        for (Map<String, ? extends Object> iterModes : this.iterationsModePercent) {
            iterModes.keySet().stream().filter(s -> !modes.contains(s)).forEach(modes::add);

        }

        return modes;
    }


    @Override
    public JFreeChart getChart() {
        return createChart();
    }

    private JFreeChart createChart() {
        DefaultCategoryDataset ds1 = createModeShareDataset();

        DefaultCategoryDataset ds2 = createPressureDataSet();

        final JFreeChart chart = ChartFactory.createBarChart(
                "People Pressured", // chart title
                "People", // domain axis label
                "Iteration", // range axis label
                ds1, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips?
                false // URL generator? Not required...
        );

        ValueAxis yAxis1 = this.axisBuilder.createValueAxis(yLabel1);
        ValueAxis yAxis2 = this.axisBuilder.createValueAxis(yLabel2);
        yAxis1.setRange(0, 1);
        CategoryAxis xAxis = this.axisBuilder.createCategoryAxis(xLabel);
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);

        DgColorScheme colorScheme = new DgColorScheme();

        GradientPaint gradient1 =
                new GradientPaint(0.0f, 0.0f, new Color(51, 104, 155, 49),
                        0.0f, 0.0f, new Color(51, 104, 155, 64));

        GradientPaint gradient2 =
                new GradientPaint(0.0f, 0.0f, new Color(80, 155, 210, 129),
                        0.0f, 0.0f, new Color(80, 155, 210, 64));


        GradientPaint gradient3 = new GradientPaint(0.0f, 0.0f,
                new Color(110, 200, 255, 255),
                0.0f, 0.0f, new Color(110, 200, 255, 64));


        final CategoryPlot plot = chart.getCategoryPlot();

        plot.setDomainAxis(xAxis);
        plot.setDrawSharedDomainAxis(true);

        plot.setRangeAxes(new ValueAxis[]{yAxis1, yAxis2});

        BarRenderer renderer1 = new StackedBarRenderer(true);
        renderer1.setSeriesPaint(0, gradient3);
        renderer1.setSeriesPaint(1, gradient2);
        renderer1.setSeriesPaint(2, gradient1);


        Shape cross = ShapeUtilities.createDiagonalCross(4, 1);
        LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
        renderer2.setSeriesLinesVisible(0, true);
        renderer2.setSeriesShapesVisible(0, true);
        renderer2.setSeriesStroke(0, new BasicStroke(5));
        renderer2.setSeriesShape(0, cross);

        renderer2.setSeriesPaint(0, colorScheme.COLOR3B);

        plot.setDataset(0, ds1);
        plot.setDataset(1, ds2);
        plot.setRenderer(0, renderer1);
        plot.setRenderer(1, renderer2);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        renderer1.setBarPainter(new StandardBarPainter());
        chart.setBackgroundPaint(ChartColor.WHITE);
        chart.getLegend().setItemFont(this.axisBuilder.getAxisFont());
        chart.setTextAntiAlias(true);
        return chart;
    }

    private DefaultCategoryDataset createPressureDataSet() {
        DefaultCategoryDataset ds2 = new DefaultCategoryDataset();

        for (int i = 0; i < iterations.size(); i++) {

            double value = peoplePressured.get(i);
            if (value == Double.NaN || Double.isInfinite(value)) {
                log.warn("Received NA or infinite value for pressure. Setting to zero. Check data for accuracy");
                value = 0;
            }
            ds2.addValue(value, "pressure", String.valueOf(iterations.get(i)));
        }
        return ds2;
    }

    private DefaultCategoryDataset createModeShareDataset() {
        DefaultCategoryDataset ds1 = new DefaultCategoryDataset();

        for (int i = 0; i < iterations.size(); i++) {

            for (String s : getUsedModes()) {
                final Map<String, Double> stringCounterMap = iterationsModePercent.get(i);
                ds1.addValue(stringCounterMap.get(s), s, String.valueOf(iterations.get(i)));
            }
        }
        return ds1;
    }


}
