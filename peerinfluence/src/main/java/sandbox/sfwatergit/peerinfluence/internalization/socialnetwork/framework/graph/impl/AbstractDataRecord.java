package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl;

import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute.Attribute;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.api.DataRecord;

import java.util.Map;
import java.util.Optional;

/**
 * Created by sidneyfeygin on 12/9/15.
 */
public class AbstractDataRecord implements DataRecord {

    final String id;

    Map<String, Attribute> attributes;

    public AbstractDataRecord(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public Optional<Attribute> getAttribute(String name) {
        return Optional.of(attributes.get(name));
    }


}
