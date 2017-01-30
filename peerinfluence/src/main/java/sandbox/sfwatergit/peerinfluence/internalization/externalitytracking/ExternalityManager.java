package sandbox.sfwatergit.peerinfluence.internalization.externalitytracking;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import sandbox.sfwatergit.peerinfluence.internalization.ExternalityType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Should receive data from marginal externality event handlers
 * <p>
 * Tracks the externality
 * Created by sidneyfeygin on 1/8/16.
 */
@Singleton
public class ExternalityManager {

    private final Map<ExternalityType,MarginalExternalityHandler> handlers = new HashMap<>();

    @Inject
    public ExternalityManager() {
    }

    public void addExternalityHandler(ExternalityType externalityType, MarginalExternalityHandler externalityHandler) {
        handlers.put(externalityType,externalityHandler);
    }

    public EnumMap<ExternalityType, Double>  assignPersonExternalityData(Id<Person> personId){
        EnumMap<ExternalityType, Double> personExternalityData = Maps.newEnumMap(ExternalityType.class);
        for (Map.Entry<ExternalityType, MarginalExternalityHandler> entry : handlers.entrySet()) {
            personExternalityData.put(entry.getKey(),entry.getValue().getPersonExternalityAmount(personId).orElse(0.0D));
        }
        return personExternalityData;
    }

    double getPersonExternalityPenalty(final Id<Person> personId, ExternalityType type){
        return Optional.ofNullable(handlers.get(type).getPersonExternalityPenalty(personId)).orElse(0.0);
    }


    public void removeExternalityHandler(ExternalityType externalityType) {
        handlers.remove(externalityType);
    }



}
