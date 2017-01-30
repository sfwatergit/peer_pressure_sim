/* *********************************************************************** *
 * project: org.matsim.*
 * DgAxisFactory
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package sandbox.sfwatergit.analysis.charts;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;

import java.awt.*;

public interface DgAxisBuilder {

    CategoryAxis createCategoryAxis(String xLabel);

    ValueAxis createValueAxis(String yLabel);

    Font getLabelFont();

    Font getAxisFont();
}