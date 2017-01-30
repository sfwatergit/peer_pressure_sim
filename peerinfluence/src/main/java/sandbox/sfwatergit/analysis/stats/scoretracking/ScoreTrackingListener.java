/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package sandbox.sfwatergit.analysis.stats.scoretracking;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.utils.collections.MapUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.UncheckedIOException;
import sandbox.sfwatergit.utils.PlanUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author thibautd
 */
@Singleton
public class ScoreTrackingListener implements StartupListener, ShutdownListener, IterationEndsListener {
	private final Map<Id<Person>, PersonScoreTracker> trackers = new HashMap<>();

	private final OutputDirectoryHierarchy io;
	private BufferedWriter writer = null;

	@Inject
	public ScoreTrackingListener(OutputDirectoryHierarchy io) {
		this.io = io;
	}

	public void addScoringFunction(
			final Id<Person> person ,
			final NamedBasicScoring scoring ) {
		addScoringFunction( person , scoring.getName() , scoring );
	}

	public void addScoringFunction(
			final Id<Person> person ,
			final String name,
			final NamedBasicScoring scoring ) {
		final PersonScoreTracker tracker =
				MapUtils.getArbitraryObject(
					person,
					trackers,
						PersonScoreTracker::new);
		tracker.addScoringFunction( name , scoring );
	}



	@Override
	public void notifyIterationEnds(final IterationEndsEvent event) {
		final String iter = ""+event.getIteration();

		try {
			for ( Map.Entry<Id<Person>, PersonScoreTracker> e : trackers.entrySet() ) {
				final Id<Person> personId = e.getKey();
				final String modeOfSelectedPlan = PlanUtils.getModeOfSelectedPlan(PlanUtils.getPerson(event.getServices().getScenario(), personId));
				final String person = personId.toString();

				for ( Map.Entry<String, Double> scores : e.getValue().getDecomposedScoring().entrySet() ) {
					writer.newLine();
					writer.write( iter );
					writer.write("\t");
					writer.write( person );
					writer.write("\t");
					writer.write(modeOfSelectedPlan);
					writer.write("\t");
					writer.write( scores.getKey() );
					writer.write("\t");
					writer.write( scores.getValue().toString() );
				}
			}
			writer.flush();
		}
		catch (IOException e) {
			throw new UncheckedIOException( e );
		}

		trackers.clear();
	}

	@Override
	public void notifyShutdown(final ShutdownEvent event) {
		try {
			writer.close();
		}
		catch (IOException e) {
			throw new UncheckedIOException( e );
		}
	}

	@Override
	public void notifyStartup(final StartupEvent event) {
		writer = IOUtils.getBufferedWriter( io.getOutputFilename( "scoringElements.dat.gz" ) );
		try {
			writer.write( "Iteration\tpersonId\tmode\tscoringElement\tscore");
		}
		catch (IOException e) {
			throw new UncheckedIOException( e );
		}
	}


}
