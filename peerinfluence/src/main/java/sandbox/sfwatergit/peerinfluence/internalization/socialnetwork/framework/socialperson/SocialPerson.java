package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

/**
 * Created by sidneyfeygin on 12/2/15.
 */
public class SocialPerson {

    private Id<Person> id;

    public SocialPerson(Id<Person> id) {
        this.id=id;
    }

    public Id<Person> getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "SocialPerson{" +
                "personId=" + id.toString() +
                '}';
    }


}
