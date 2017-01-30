package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute;

/**
 * Created by sidneyfeygin on 12/9/15.
 */
public interface Attribute<V> {

    /**
     * @return the attribute key
     */
    String key();

    /**
     * @return the attribute value
     */
    V value();

    
}
