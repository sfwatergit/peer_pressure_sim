/* *********************************************************************** *
 * project: org.matsim.*
 * FilterCordonLinksFromLinkStats.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package sandbox.sfwatergit.analysis.scripts;

import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FilterBridgeLinksFromLinkStats {

    private static ArrayList<String> bridgeLinks = new ArrayList<String>();
    private static ArrayList<String> bridgeLinkStats = new ArrayList<String>();

    private static String run = "1-01";
    private static Integer[] iters = new Integer[]{140, 160, 180};

    public static void main(String[] args) throws IOException {

        for (Integer iter : iters) {
            System.out.println("Read bridge link ids...");
            String bridgeLinkFile = "/Users/sidneyfeygin/current_code/data/shapefile/bridge_links.txt";
            readBridgeLinkFile(bridgeLinkFile);
            System.out.println("... done.");
            System.out.println("# bridge links = " + bridgeLinks.size());
            System.out.println();

            System.out.println("Read linkstats and save lines containing the bridge links ...");
            String linkStatsFile = String.format("/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources/output_archives/sf_bay/bart_caltrain/ITERS/it.%2$s/%1$s.%2$s.linkstats.txt.gz", run, iter);
            readBridgeLinkStats(linkStatsFile);
            System.out.println("... done.");
            System.out.println("# bridge links = " + bridgeLinkStats.size());
            System.out.println();

            System.out.println("Write linkstats for  bay bridge links ...");
            String bridgeLinkStatsFile = String.format("/Users/sidneyfeygin/current_code/java/matsim_home/ucb_smartcities_all/sandbox/sfwatergit/src/main/resources/output_archives/sf_bay/bart_caltrain/ITERS/it.%1$s/%1$s.it.140.it.160.it.200.bridgelinkstats.txt", iter);
            writeBridgeLinkStats(bridgeLinkStatsFile);
            System.out.println("... done.");
            System.out.println();
        }
    }


    public static void readBridgeLinkFile(String bridgeLinkFile) throws IOException {

        BufferedReader in = IOUtils.getBufferedReader(bridgeLinkFile);
        in.readLine();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String[] entries = inputLine.split("\t");
            bridgeLinks.add(entries[0]);
        }
    }


    public static void readBridgeLinkStats(String bridgeLinkFile) throws IOException {

        BufferedReader in = IOUtils.getBufferedReader(bridgeLinkFile);
        bridgeLinkStats.add(in.readLine());
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String[] entries = inputLine.split("\t");
            if (bridgeLinks.contains(entries[0])) {
                bridgeLinkStats.add(inputLine);
            }
        }
    }

    public static void writeBridgeLinkStats(String bridgeLinkStatsFile) throws IOException {

        BufferedWriter out = IOUtils.getBufferedWriter(bridgeLinkStatsFile);
        for (String line : bridgeLinkStats) {
            out.write(line);
            out.newLine();
        }
        out.close();
    }
}
