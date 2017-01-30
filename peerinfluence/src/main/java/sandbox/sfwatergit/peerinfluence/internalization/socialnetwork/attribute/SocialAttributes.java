package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute;

import java.util.EnumMap;

/**
 * TODO: If we don't need these, get rid of 'em
 * Created by sidneyfeygin on 6/19/15.
 */
public class SocialAttributes {

    // EnumMap of general attributes
    private EnumMap<AttributeKind, Double> mapping;

    public SocialAttributes() {
        if (mapping == null) {
            mapping = new EnumMap<>(AttributeKind.class);
        }
    }

    public Double getValue(AttributeKind z) {
        if (mapping == null) {
            mapping = new EnumMap<>(AttributeKind.class);
        }
        return mapping.get(z);
    }

    public void setValue(AttributeKind z, Double value) {
        mapping.put(z, value);
    }
}
