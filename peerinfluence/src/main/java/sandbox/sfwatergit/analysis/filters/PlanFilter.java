package sandbox.sfwatergit.analysis.filters;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.BasicPlan;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sidneyfeygin on 6/4/16.
 */
public class PlanFilter {

    private static void writeHeader(BufferedWriter bw) {
        try {
            bw.write("ID\tScore\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        PopulationReader populationReader = new PopulationReader(sc);
        populationReader.readFile(args[0]);
        final Population population = sc.getPopulation();
        final Map<Id<Person>, Double> output = population.getPersons().values().stream().flatMap(p -> p.getPlans().stream()).filter(p -> p.getScore() < -1000).collect(Collectors.toMap(k -> k.getPerson().getId(), BasicPlan::getScore));
        final BufferedWriter abw = IOUtils.getAppendingBufferedWriter(args[1]);
        writeHeader(abw);
        writeData(abw, output);
        try {
            abw.flush();
            abw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeData(BufferedWriter abw, Map<Id<Person>, Double> output) {
        output.entrySet().stream().forEach(e -> {
            try {
                abw.write(String.format("%s\t%s\n", e.getKey().toString(), String.valueOf(e.getValue())));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }
}
