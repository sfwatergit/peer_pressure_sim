package sandbox.sfwatergit.peerinfluence.run.config;


import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.core.utils.misc.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Purpose of Class:
 * <p>
 * matsim_smartcities ==> edu.berkeley.smartcities.sf.travelTime.social_network.io
 * Date: 3/17/15
 * Time: 3:15 AM
 * Version: 1.0
 *
 * @author ${AUTHOR}
 */
public class SocialNetworkConfigGroup extends ReflectiveConfigGroup {
    public static final String GROUP_NAME = "socialNetworkModule";
    public static final String SN_FILE_ELEMENT_NAME = "socialnetworkfile";
    private static final Logger log = Logger.getLogger(ReflectiveConfigGroup.class);
    private String socialNetworkFile = "";


    public SocialNetworkConfigGroup() {
        super(GROUP_NAME);
    }

    @Override
    public ConfigGroup createParameterSet(final String type) {
        if (type.equals(StrategyParameterSet.SET_NAME)) {
            return new StrategyParameterSet();
        }
        throw new IllegalArgumentException(type);
    }

    public void addStrategyParameterSet(final StrategyParameterSet set) {
        addParameterSet(set);
    }

    @SuppressWarnings("unchecked")
    public Collection<StrategyParameterSet> getStrategyParameterSets() {
        final Collection<? extends ConfigGroup> sets = getParameterSets(StrategyParameterSet.SET_NAME);
        return (Collection<StrategyParameterSet>) sets;
    }

    @StringGetter("socialnetworkfile")
    @DoNotConvertNull
    public String getSocialNetworkFile() {
        return this.socialNetworkFile;
    }

    @StringSetter("socialnetworkfile")
    @DoNotConvertNull
    public void setSocialNetworkFile(String snFile) {
        if (snFile == null) throw new IllegalArgumentException();
        this.socialNetworkFile = snFile;
    }

    public static class StrategyParameterSet extends ReflectiveConfigGroup {
        public static final String SET_NAME = "strategy";

        private String strategyName = null;
        private double weight = 0;
        private ArrayList<String> parameters = new ArrayList<>();
        private int disabledInIteration;

        public StrategyParameterSet() {
            super(SET_NAME);
        }

        @StringGetter("strategyName")
        public String getStrategyName() {
            return this.strategyName;
        }

        @StringSetter("strategyName")
        public void setStrategyName(final String strategyName) {
            this.strategyName = strategyName;
        }

        @StringGetter("weight")
        public double getWeight() {
            return this.weight;
        }

        @StringSetter("weight")
        public void setWeight(final double weight) {
            this.weight = weight;
        }

        @StringGetter("disableInIteration")
        public int getDisableInIteration() {
            return disabledInIteration;
        }

        @StringSetter("disableInIteration")
        public void setDisableInIteration(String iteration) {
            disabledInIteration = Integer.valueOf(iteration);
        }

        @StringGetter("parameters")
        public ArrayList<String> getParameters() {
            return this.parameters;
        }

        @StringSetter("parameters")
        public void setParameters(final String parameterList) {
            final String[] explode = StringUtils.explode(parameterList, ',');
            Collections.addAll(parameters, explode);
        }

    }

    public static class StrategySettings {
        private Id<StrategySettings> id;
        private double probability = -1.0;
        private String moduleName = null;
        private String[] parameters = null;

        public StrategySettings(final Id<StrategySettings> id) {
            this.id = id;
        }

        public double getProbability() {
            return this.probability;
        }

        public void setProbability(final double probability) {
            this.probability = probability;
        }

        public String getModuleName() {
            return this.moduleName;
        }

        public void setModuleName(final String moduleName) {
            this.moduleName = moduleName;
        }

        public Id<StrategySettings> getId() {
            return this.id;
        }

        public void setId(final Id<StrategySettings> id) {
            this.id = id;
        }

        public ArrayList<String> getParametersAsArrayList() {
            ArrayList<String> list = new ArrayList<>();

            if (this.parameters != null) {
                Collections.addAll(list, this.parameters);
            }

            return list;
        }

        public String getParametersAsString() {
            StringBuilder strBuffer = new StringBuilder();

            if (this.parameters != null) {
                strBuffer.append(this.parameters[0]);
                for (int i = 1; i < this.parameters.length; i++) {
                    strBuffer.append(",");
                    strBuffer.append(this.parameters[i]);
                }
            }

            return strBuffer.toString();
        }

        public void setParameters(String parameter) {
            if (!parameter.equalsIgnoreCase("")) {
                String[] parts = StringUtils.explode(parameter, ',');
                this.parameters = new String[parts.length];
                for (int i = 0, n = parts.length; i < n; i++) {
                    this.parameters[i] = parts[i].trim().intern();
                }
            }
        }

    }

}
