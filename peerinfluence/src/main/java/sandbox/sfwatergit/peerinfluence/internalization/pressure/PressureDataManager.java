package sandbox.sfwatergit.peerinfluence.internalization.pressure;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import gnu.trove.TDoubleArrayList;
import lombok.Getter;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.ScoringEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.ScoringListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.utils.collections.MapUtils;
import sandbox.sfwatergit.analysis.modules.legMode.ModalShareUtils;
import sandbox.sfwatergit.analysis.runtime.BasicAnalysis;
import sandbox.sfwatergit.analysis.stats.PressureAnalysisUtils;
import sandbox.sfwatergit.analysis.stats.StatKind;
import sandbox.sfwatergit.peerinfluence.internalization.ExternalityType;
import sandbox.sfwatergit.peerinfluence.internalization.pressure.algorithm.PressurePersonData;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.peerinfluence.run.config.PeerPressureAnalysisConfigGroup;
import sandbox.sfwatergit.utils.PlanUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages all operations on {@link PressurePersonData}
 * Created by sidneyfeygin on 1/9/16.
 */
@Singleton
public class PressureDataManager implements StartupListener, ScoringListener, AfterMobsimListener, ShutdownListener {
    static private final Logger log = Logger.getLogger(PressureDataManager.class);

    //========= PRIVATE FIELDS ====================

    private PressureAnalysisUtils pressureAnalysisUtils = new PressureAnalysisUtils();
    private final int startPressureIter;

    private final double marginalPressureCost;
    private final OutputDirectoryHierarchy controlerIO;
    private final String[] modes;
    private final Map<Id<Person>, PressurePersonData> pressureDataMap = Maps.newHashMap();
    private final TreeMap<Integer, IterationDataRecord> iterationDataMap = new TreeMap<>();
    private final MapUtils.Factory<IterationDataRecord> iterationDataFactory = IterationDataRecord::new;
    private final Scenario scenario;
    private int iteration;
    private final SocialNetwork socialNetwork;


    //========= CONSTRUCTOR ====================

    @Inject
    PressureDataManager(Scenario scenario,
                        @Named(PeerPressureAnalysisConfigGroup.MODE_TYPES) String modes,
                        @Named(PeerPressureAnalysisConfigGroup.START_PRESSURE_ITERATION) int startPressureIter,
                        @Named(PeerPressureAnalysisConfigGroup.PRESSURE_COST) double marginalPressureCost,
                        SocialNetwork socialNetwork,
                        OutputDirectoryHierarchy controlerIO) {
        this.startPressureIter = startPressureIter;

        this.modes = modes.split(",");
        this.marginalPressureCost = marginalPressureCost;
        this.controlerIO = controlerIO;
        this.scenario = scenario;
        this.socialNetwork = socialNetwork;
    }

    //========= PUBLIC METHODS ====================

    public Map<Id<Person>, PressurePersonData> getPressurePersonData() {
        return pressureDataMap;
    }

    public void addExternalityData(Id<Person> personId, EnumMap<ExternalityType, Double> externalityData) {
        pressureDataMap.get(personId).setExternality(externalityData);
    }

    //========= INHERITED METHODS ====================

    @Override
    public void notifyStartup(StartupEvent event) {
        // Create pressure data cache from loaded social network
        for (SocialVertex socialVertex : socialNetwork.getVertices()) {
            pressureDataMap.put(socialVertex.getId(), new PressurePersonData());
        }

    }

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {
        // Update iteration here first
        iteration = event.getIteration();
        updatePressurePersonData();
        if(this.iteration >startPressureIter) {
            pressureDataMap.keySet().forEach(this::assignIndividualPressurePenalty);
        }
    }

    @Override
    public void notifyScoring(ScoringEvent event) {
        updateIterationDataRecords();
    }

    @Override
    public void notifyShutdown(ShutdownEvent event) {
        log.info("Beginning dump analytics...");
        TDoubleArrayList iterationArray = new TDoubleArrayList(iterationDataMap.keySet().stream().mapToDouble(p->(double)p).sorted().toArray());
        final BasicAnalysis ba = new BasicAnalysis(scenario,event.getServices().getControlerIO(),iterationArray, iterationDataMap.values());

        ba.writeChart();
        ba.writePressureDataToFile(scenario.getPopulation().getPersons().size());

        final String runId = Optional.ofNullable(event.getServices().getConfig().controler().getRunId()).orElse("1");

        PressureAnalysisUtils.writeDescriptiveStats(controlerIO.getOutputPath()+"/"+runId+".", iterationDataMap);
        log.info("Finished dump analytics...");
    }

    //========= PRIVATE METHODS ====================


    private void updatePressurePersonData() {
        for (Map.Entry<Id<Person>, PressurePersonData> ppde : pressureDataMap.entrySet()) {
            final Person person = scenario.getPopulation().getPersons().get(ppde.getKey());
            final Plan selectedPlan = Optional.ofNullable(person.getSelectedPlan()).orElse(scenario.getPopulation().getFactory().createPlan());
            final Double selectedPlanScore = Optional.ofNullable(selectedPlan.getScore()).orElse(Double.NaN);

            // might happen on first (zeroth) iteration, but otherwise let us know... could be bug.
            if (selectedPlanScore == Double.NaN) {
                if (iteration > 0)
                    log.warn(String.format("NaN score for agent %s", person.getId().toString()));
                continue;
            }

            final PressurePersonData ppd = ppde.getValue();

            ppd.setSelectedPlanScore(selectedPlanScore);
            ppd.putModeData(iteration, PlanUtils.getModeOfSelectedPlan(person));
            ppd.clearPressurePenalty();

            // Update best plan scores here:
            final Map<String, Double> bestPlanScores = ppd.getBestPlanScores();

            for (String mode : modes) {
                double bestModeScore = bestPlanScores.get(mode);
                final double currentIterBestModeScore = PlanUtils.getBestModeScore(person.getPlans(), mode);
                bestModeScore = currentIterBestModeScore > bestModeScore ? currentIterBestModeScore : bestModeScore;
                bestPlanScores.replace(mode, bestModeScore);
            }
        }
    }

    private void assignIndividualPressurePenalty(Id<Person> egoId) {
        final SocialVertex egoVertex = socialNetwork.getSocialVertices().get(egoId);
        pressureEgo(egoVertex, socialNetwork.getNeighbors(egoVertex));
    }

    private void pressureEgo(SocialVertex ego, Collection<SocialVertex> neighbors) {
        // Invariant: ego must be driving
        final Person egoPerson = PlanUtils.getPerson(scenario, ego.getId());
        final Plan egoSelectedPlan = egoPerson.getSelectedPlan();
        if (egoSelectedPlan.getType() != null)
            if ((egoSelectedPlan.getType().equals(FlaggedPlanModifier.FLAGGED) || egoSelectedPlan.getType().equals(FlaggedPlanModifier.SHIFTED))) {
                return;
            }
        double total = 0.0;
        Set<SocialVertex> pressuringNeighbors = Sets.newHashSet();
        final PressurePersonData egoData = pressureDataMap.get(ego.getId());
        final Double egoUtilityDiff = Math.abs(egoData.getUtilityDiff());
        final String egoMode = PlanUtils.getModeOfSelectedPlan(PlanUtils.getPerson(scenario, egoPerson.getId()));

        if(!(PlanUtils.isDriving(scenario,egoPerson.getId()))||egoUtilityDiff.isInfinite()){
            return;
        }
        for (SocialVertex neighbor : neighbors) {
            final Id<Person> alterId = neighbor.getId();
            final PressurePersonData alterData = pressureDataMap.get(alterId);
            final Double alterUtilityDiff = Math.abs(alterData.getUtilityDiff());
            String alterMode = PlanUtils.getModeOfSelectedPlan(PlanUtils.getPerson(scenario,alterId));
            if(alterMode.equals(TransportMode.pt)||alterMode.equals(TransportMode.walk)||alterMode.equals(TransportMode.transit_walk)){
                alterData.addToPressurePenalty(marginalPressureCost*egoUtilityDiff);
                total+=egoUtilityDiff;
                pressuringNeighbors.add(neighbor);
                // else if alter is also driving and driving utility of ego < utility of alter
            }else {
                if(egoUtilityDiff<alterUtilityDiff){
                    alterData.addToPressurePenalty(marginalPressureCost*egoUtilityDiff);
                    total+=egoUtilityDiff;
                    pressuringNeighbors.add(neighbor);
                }
            }
        }
        egoData.setPressuredBy(pressuringNeighbors);
        if(total>egoUtilityDiff){
            egoData.addToPressurePenalty(egoUtilityDiff);
            egoSelectedPlan.setType(FlaggedPlanModifier.FLAGGED);
        }
    }


    private void updateIterationDataRecords() {
        final IterationDataRecord iterationRecord = iterationDataFactory.create();
        iterationDataMap.put(iteration, iterationRecord);
        iterationRecord.updateStats();
        iterationRecord.updateModeShareData(scenario.getPopulation());
        iterationRecord.setNumPeoplePressured(iteration);
    }




    //========= INNER CLASS ====================


    public class IterationDataRecord {

        @Getter
        private Map<String, Double> iterationModeShareData = Maps.newHashMap();

        @Getter
        private Map<StatKind, DescriptiveStatistics> iterationStatsHistory = Maps.newHashMap();

        @Getter
        private Long numPeoplePressured = 0L;



        IterationDataRecord() {
            iterationModeShareData = new HashMap<>();
        }

        void updateStats() {
            iterationStatsHistory = Stream.of(StatKind.values()).collect(
                    Collectors.toMap(
                            k -> k,
                            v -> PressureAnalysisUtils.STAT_CONVERTER_SUPPLIER_MAP
                                    .get(v)
                                    .getStatistics(pressureDataMap.values(), v)));
        }

        void updateModeShareData(Population population) {
            iterationModeShareData = ModalShareUtils.getMode2PctShareFromPlans(population);
        }


        void setNumPeoplePressured(int iteration) {
            numPeoplePressured = pressureDataMap.values().stream().filter(p->p.wasPressured(iteration)).count();
        }
    }


}
