package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.api;

import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute.Attribute;

import java.util.Map;
import java.util.Optional;

/**
 * Created by sidneyfeygin on 12/9/15.
 */
public interface DataRecord {
    String getId();

    Map<String, Attribute> getAttributes();

    Optional<Attribute> getAttribute(String name);

}
