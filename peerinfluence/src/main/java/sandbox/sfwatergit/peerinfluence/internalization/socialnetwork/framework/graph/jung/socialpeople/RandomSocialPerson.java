package sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.graph.jung.socialpeople;

import org.matsim.api.core.v01.Id;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.framework.socialperson.SocialPerson;

import java.util.Random;

/**
 * Created by sidneyfeygin on 1/16/16.
 */
public class RandomSocialPerson extends SocialPerson {

    public RandomSocialPerson(Random random) {
        super(Id.createPersonId(random.nextInt()));
    }

}
