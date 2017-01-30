/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
package sandbox.sfwatergit.peerinfluence.internalization.ghg;

/**
 * CO2 value from
 * <a href=http://www3.epa.gov/climatechange/EPAactivities/economics/scc.html>USEPA: The Social Cost of Carbon</a>
 * Retrieved Nov., 2015
 *
 * @author sfeygin based on amit
 */
public enum EmissionCostFactors {

    CO2_TOTAL(120. / (1000. * 1000.)); // $US Per gram CO_2

    private double costFactors;

    EmissionCostFactors(double costFactor) {
        this.costFactors = costFactor;
    }

    public double getCostFactor() {
        return costFactors;
    }
}
