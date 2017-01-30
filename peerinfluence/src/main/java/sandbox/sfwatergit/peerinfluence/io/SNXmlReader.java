/* *********************************************************************** *
 * project: org.matsim.*
 * SocialNetworkReader.java
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
package sandbox.sfwatergit.peerinfluence.io;

import com.google.inject.Inject;
import edu.uci.ics.jung.graph.SparseMultigraph;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetworkImpl;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute.AttributeKind;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson.SocialPerson;

import java.util.Stack;

/**
 * @author thibautd
 */
public class SNXmlReader extends MatsimXmlParser implements SNReader {
    public static final String TYPE = "xml";
    private final Scenario scenario;
    private final String elementName;
    private SocialNetwork socialNetwork;



    @Inject
    public SNXmlReader(final Scenario scenario) {
        this.scenario = scenario;
        this.elementName = SocialNetwork.ELEMENT_NAME;
    }

    public SNXmlReader(
            final String elementName,
            final Scenario scenario) {
        this.scenario = scenario;
        this.elementName = elementName;
    }

    @Override
    public void startTag(
            final String name,
            final Attributes atts,
            final Stack<String> context) {
        switch (name) {
            case SNXmlWriter.ROOT_TAG:
                this.socialNetwork = new SocialNetworkImpl(new SparseMultigraph<>());
                this.scenario.addScenarioElement(elementName, this.socialNetwork);
                break;
            case SNXmlWriter.EGO_TAG: {
                final SocialVertex ego = new SocialVertex(new SocialPerson(Id.create(
                        atts.getValue(
                                SNXmlWriter.EGO_ATT), Person.class)));
                this.socialNetwork.addVertex(ego);

                break;
            }
            case SNXmlWriter.TIE_TAG: {
                final SocialVertex ego = new SocialVertex(new SocialPerson(Id.create(
                        atts.getValue(
                                SNXmlWriter.EGO_ATT), Person.class)));
                final SocialVertex alter = new SocialVertex(new SocialPerson(Id.create(
                        atts.getValue(
                                SNXmlWriter.ALTER_ATT), Person.class)));
                this.socialNetwork.addRelationship(ego, alter, 0.0);
                break;
            }
            case SNXmlWriter.ATTRIBUTE_TAG:
                this.socialNetwork.setAttribute(AttributeKind.SOCIAL_NORM,
                        Double.valueOf(atts.getValue(SNXmlWriter.VALUE_ATT)));
                break;
        }
    }

    @Override
    public void endTag(
            final String name,
            final String content,
            final Stack<String> context) {
    }

    @Override
    public void read(String filename) {
        readFile(filename);
    }
}

