package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.jung;

/**
 * Created by sidneyfeygin on 1/16/16.
 */
public enum Choice {
    P("Pressure"), NP("No Pressure");

    private final String pressure;

    Choice(String pressure) {
        this.pressure = pressure;
    }

    public String pressure() {
        return pressure;
    }
}
