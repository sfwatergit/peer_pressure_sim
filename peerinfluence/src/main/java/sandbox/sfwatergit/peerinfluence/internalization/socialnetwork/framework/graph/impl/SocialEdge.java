package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl;

import com.google.common.base.Objects;
import org.matsim.api.core.v01.Id;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute.AttributeKind;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.attribute.SocialAttributes;

/**
 * Purpose of Class:
 * <p>
 * matsim_smartcities ==> edu.berkeley.smartcities.sf.travelTime.social_network.struct
 * Date: 3/12/15
 * Time: 10:26 PM
 * Version: 1.0
 *
 * @author ${AUTHOR}
 */
public class SocialEdge extends EdgeImpl {

    Id mRelId;

    SocialAttributes attributes = new SocialAttributes();

    public SocialEdge(String mRelId) {
        this.mRelId = Id.create(mRelId, SocialEdge.class);
    }

    public SocialEdge(SocialVertex ego, SocialVertex alter) {
        super(ego, alter);

        mRelId = Id.create(this.vertices.getFirst().toString() + "-" + this.vertices.getSecond().toString(), SocialEdge.class);
    }

    public SocialEdge(SocialVertex ego, SocialVertex alter, Double tieStrength) {
        this(ego, alter);
        setTieStrength(tieStrength);
    }

    public SocialEdge() {

    }

    /**
     * @return tieStrength attribute representing the closeness of the pair.
     */
    public Double getTieStrength() {
        Double res = 0.0;
        try {
            res = attributes.getValue(AttributeKind.PT_PROB);
        } catch (NullPointerException e) {
            e.initCause(new Exception("No tie found for " + mRelId));
        }
        return res;
    }

    public void setTieStrength(Double tieStrength) {
        if (tieStrength > 1.0 || tieStrength < 0) {
            throw new RuntimeException("Value out of bounds for a probability");
        }
        this.attributes.setValue(AttributeKind.PT_PROB, tieStrength);
    }

    @Override
    public String toString() {
        return String.format("ID: %s", (this.vertices.getFirst().toString() + "-" + this.vertices.getSecond().toString()));
    }


    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(mRelId, this.vertices.getFirst(), this.vertices.getSecond(), attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocialEdge that = (SocialEdge) o;

        return Objects.equal(this.mRelId, that.mRelId) &&
                Objects.equal(this.vertices.getFirst(), that.vertices.getFirst()) &&
                Objects.equal(this.vertices.getSecond(), that.vertices.getSecond()) &&
                Objects.equal(this.attributes, that.attributes);
    }

    public SocialVertex getAlter() {
        return (SocialVertex) this.vertices.getSecond();
    }

    public SocialVertex getEgo() {
        return (SocialVertex) this.vertices.getFirst();
    }


}
