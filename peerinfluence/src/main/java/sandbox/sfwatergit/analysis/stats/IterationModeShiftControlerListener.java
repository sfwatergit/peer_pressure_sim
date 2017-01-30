package sandbox.sfwatergit.analysis.stats;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.ControlerListener;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.PressureDataManager;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson.SocialPerson;
import sandbox.sfwatergit.peerinfluence.io.IterationSummaryFileControlerListener;
import sandbox.sfwatergit.peerinfluence.io.StreamingOutput;
import sandbox.sfwatergit.utils.PlanUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class IterationModeShiftControlerListener implements Provider<ControlerListener> {

    private final OutputDirectoryHierarchy controlerIO;
    private final PressureDataManager pressureDataManager;


    @Inject
    public IterationModeShiftControlerListener(OutputDirectoryHierarchy controlerIO, PressureDataManager pressureDataManager) {
        this.controlerIO = controlerIO;
        this.pressureDataManager = pressureDataManager;

    }

    @Override
    public ControlerListener get() {

        Map<String, IterationSummaryFileControlerListener.Writer> fileWriters = Maps.newHashMap();
        fileWriters.put("modeshifters.txt", new IterationSummaryFileControlerListener.Writer() {

            @Override
            public StreamingOutput notifyStartup(StartupEvent event) {
                return pw -> pw.printf("%s\t%s\t%s\t%s\t%s\n", "personId", "shift_direction", "iteration", "was_shifted", "pressuredBy");
            }


            @Override
            public StreamingOutput notifyIterationEnds(IterationEndsEvent event) {
                final HashSet<SocialVertex> none = Sets.newHashSet();
                none.add(new SocialVertex(new SocialPerson(Id.createPersonId("NA"))));

                return pw -> pressureDataManager.getPressurePersonData().entrySet().forEach(ppde -> {
                    final int iteration = event.getIteration();


                    final Optional<Set<SocialVertex>> pressuredBy = Optional.of(ppde.getValue().getPressuredBy());
                    final Set<String> peeps = pressuredBy.orElse(none).stream().map(SocialVertex::toString).collect(Collectors.toSet());

                    boolean wasPressured = ppde.getValue().wasPressured(iteration);
                    String peepString;
                    if (wasPressured)
                        if (!peeps.isEmpty()) {
                            PlanUtils.getPerson(event.getServices().getScenario(), ppde.getKey()).getSelectedPlan().setType(null);
                            peepString = Joiner.on(',').join(peeps);
                            ppde.getValue().setPressuredBy(new HashSet<>());
                        } else {
                            peepString = "NA";
                            ppde.getValue().setShifted(iteration, false);
                            wasPressured = false;
                        }
                    else {
                        peepString = "NA";
                        ppde.getValue().setShifted(iteration, false);
                        wasPressured = false;
                    }
                    final String modeShift = ppde.getValue().getModeShift(iteration);
                    pw.printf("%s\t%s\t%s\t%s\t%s\n", ppde.getKey().toString(), modeShift, String.valueOf(iteration), String.valueOf(wasPressured), peepString);
                });
            }
        });

        return new IterationSummaryFileControlerListener(controlerIO, fileWriters);
    }
}
