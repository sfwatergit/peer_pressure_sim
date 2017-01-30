package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.api.Vertex;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson.SocialPerson;

/**
 * A vertex implementation that includes a SocialPerson delegate.
 *
 * Created by sidneyfeygin on 12/2/15.
 */

public class SocialVertex implements Vertex {

    private SocialPerson socialPerson;


    public SocialVertex() {
    }

    @Inject
    public SocialVertex(SocialPerson socialPerson) {
        this.socialPerson = socialPerson;
    }

    public SocialPerson getSocialPerson() {
        return socialPerson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocialVertex)) return false;
        SocialVertex that = (SocialVertex) o;
        return Objects.equal(socialPerson.getId(), that.socialPerson.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(socialPerson.getId());
    }



    @Override
    public String toString() {
        return socialPerson.getId().toString();
    }


    public Id<Person> getId() {
        return socialPerson.getId();
    }
}
