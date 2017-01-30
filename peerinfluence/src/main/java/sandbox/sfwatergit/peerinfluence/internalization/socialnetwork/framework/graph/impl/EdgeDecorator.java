package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl;

/**
 * Created by sidneyfeygin on 1/28/16.
 */
public class EdgeDecorator extends EdgeImpl {

    private final EdgeImpl delegate;

    public EdgeDecorator(EdgeImpl delegate) {
        this.delegate = delegate;
    }


}
