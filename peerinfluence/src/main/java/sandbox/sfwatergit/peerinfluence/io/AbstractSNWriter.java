package sandbox.sfwatergit.peerinfluence.io;

import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.utils.io.AbstractMatsimWriter;
import sandbox.sfwatergit.peerinfluence.internalization.socialnetwork.SocialNetwork;

import java.io.IOException;

/**
 * Use to toFile social networks to file
 * <p>
 * Created by sidneyfeygin on 5/14/15.
 */
public abstract class AbstractSNWriter extends AbstractMatsimWriter implements MatsimWriter {

    //////////////////////////////////////////////////////////////////////
    // member variables
    //////////////////////////////////////////////////////////////////////

    private final SocialNetwork socialNetwork;


    public AbstractSNWriter(SocialNetwork socialNetwork) {
        this.socialNetwork = socialNetwork;
    }


    //////////////////////////////////////////////////////////////////////
    // constructors
    //////////////////////////////////////////////////////////////////////


    @Override
    public void write(String filename) {
        try {
            writeStart(filename);
            writeSocialNetwork();
            writeFinish();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeFinish() {
        try {
            this.writer.flush();
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void writeSocialNetwork() throws IOException;


    protected abstract void writeStart(String filename) throws IOException;

}
