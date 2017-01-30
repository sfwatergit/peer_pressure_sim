package sandbox.sfwatergit.analysis.charts;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;

import java.awt.*;

public class DgDefaultAxisBuilder implements DgAxisBuilder {


    private Font labelFont = new Font("Helvetica", Font.BOLD, 18);
    private Font axisFont = new Font("Helvetica", Font.BOLD, 14);


    public CategoryAxis createCategoryAxis(String xLabel) {
        CategoryAxis categoryAxis = new CategoryAxis(xLabel);
        categoryAxis.setCategoryMargin(0.05); // percentage of space between categories
        categoryAxis.setLowerMargin(0.01); // percentage of space before first bar
        categoryAxis.setUpperMargin(0.01); // percentage of space after last bar
        categoryAxis.setLabelFont(labelFont);
        categoryAxis.setTickLabelFont(axisFont);
        return categoryAxis;
    }

    public ValueAxis createValueAxis(String yLabel) {
        ValueAxis valueAxis = new NumberAxis(yLabel);
        valueAxis.setLabelFont(labelFont);
        valueAxis.setTickLabelFont(axisFont);
        return valueAxis;
    }


    public Font getLabelFont() {
        return labelFont;
    }


    public Font getAxisFont() {
        return axisFont;
    }

}