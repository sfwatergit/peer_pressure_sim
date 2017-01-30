package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.impl.SocialVertex;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson.SocialPerson;

/**
 * Created by sidneyfeygin on 12/2/15.
 */
public class SocialNetworkUtils {

    private SocialNetworkUtils() {
    }

    public static SocialVertex createSocialVertex(Id<Person> personId) {
        return new SocialVertex(new SocialPerson(personId));
    }

    public static SocialVertex createSocialVertex(Person person) {
        return new SocialVertex(new SocialPerson(person.getId()));
    }

    public static SocialVertex createSocialVertex(long id) {
        return createSocialVertex(Id.createPersonId(id));
    }

    public static SocialVertex createSocialVertex(String id) {
        return createSocialVertex(Id.createPersonId(id));
    }



}
