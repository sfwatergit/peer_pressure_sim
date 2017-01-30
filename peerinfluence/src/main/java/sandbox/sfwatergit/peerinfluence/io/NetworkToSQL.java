package sandbox.sfwatergit.peerinfluence.io;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;
import sandbox.sfwatergit.utils.postgresql.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Transforms a MATSim network to SQL for map matching
 * Created by sidneyfeygin on 1/21/16.
 */
public class NetworkToSQL {
    public static final String LINK_TABLE = "linkdetails";
    public static final String NODE_TABLE = "nodes";
    public static final double FT_PER_METER = 3.2825D;
    private final String netFile;
    private String postgresProperties;
    private boolean convert;

    private NetworkToSQL(String netFile, String postgresProperties) {
        this.netFile = netFile;
        this.postgresProperties = postgresProperties;
    }

    private void run() throws ClassNotFoundException, SQLException, InstantiationException, IOException, IllegalAccessException {
        final Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        NetworkReaderMatsimV2 netReader = new NetworkReaderMatsimV2(scenario.getNetwork());
        netReader.readFile(netFile);
        Network network = scenario.getNetwork();
        if (convert) {
            ConvertNetworkIds(network);
        }
        writeNodesToSQL(network);
        writeLinksToSQL(network);

    }

    private Network ConvertNetworkIds(Network network) {
        int range = network.getNodes().size();
        final List<Integer> sack = new ArrayList<>(range);
        for (int i = 0; i < range; i++) sack.add(i);
        Collections.shuffle(sack);
        for (Link link : network.getLinks().values()) {

        }
        return null;
    }

    private void writeLinksToSQL(Network network) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException {

        List<PostgresqlColumnDefinition> columns = new ArrayList<>();
        columns.add(new PostgresqlColumnDefinition("id",PostgresType.BIGINT));
        columns.add(new PostgresqlColumnDefinition("type",PostgresType.INT));
        columns.add(new PostgresqlColumnDefinition("source",PostgresType.BIGINT));
        columns.add(new PostgresqlColumnDefinition("destination",PostgresType.BIGINT));
        columns.add(new PostgresqlColumnDefinition("length", PostgresType.FLOAT8));

        TableWriter linkWriter;
        File file = new File(postgresProperties);
        DataBaseAdmin dba = new DataBaseAdmin(file);
        linkWriter = new PostgresqlCSVWriter("LINKWRITER", String.format("public.%s", LINK_TABLE), dba, 10000, columns);

        DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_MM");
        String formattedDate = df.format(new Date());

        linkWriter.addComment(String.format("created on %s for network_SF_BAY_detailed", formattedDate));

        for (Link link : network.getLinks().values()) {
            Object[] args = new Object[columns.size()];
            args[0] = link.getId();
            args[1] = 1;
            args[2] = link.getFromNode().getId();
            args[3] = link.getToNode().getId();
            args[4] = link.getLength() * FT_PER_METER;
            linkWriter.addLine(args);
        }
        linkWriter.finish();
    }


    private void writeNodesToSQL(Network network) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException {
        List<PostgresqlColumnDefinition> columns = new ArrayList<>();
        columns.add(new PostgresqlColumnDefinition("id",PostgresType.BIGINT));
        columns.add(new PostgresqlColumnDefinition("type", PostgresType.INT));
        columns.add(new PostgresqlColumnDefinition("x", PostgresType.FLOAT8));
        columns.add(new PostgresqlColumnDefinition("y", PostgresType.FLOAT8));


        TableWriter nodeWriter;
        File file = new File(postgresProperties);
        DataBaseAdmin dba = new DataBaseAdmin(file);
        nodeWriter = new PostgresqlCSVWriter("NODEWRITER", String.format("public.%s", NODE_TABLE), dba, 10000, columns);

        DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_MM");
        String formattedDate = df.format(new Date());

        nodeWriter.addComment(String.format("created on %s for network_SF_BAY_detailed", formattedDate));
        CoordinateTransformation transform =  new GeotoolsTransformation("EPSG:32610", "EPSG:4326");
        for (Node node : network.getNodes().values()) {
            final Coord coord = transform.transform(node.getCoord());
            Object[] args = new Object[columns.size()];
            args[0] = node.getId();
            args[1] = 1;
            args[2] = coord.getX();
            args[3] = coord.getY();
            nodeWriter.addLine(args);
        }

        nodeWriter.finish();

    }


    public static void main(String[] args) {
        final NetworkToSQL networkToSQL = new NetworkToSQL(args[0],args[1]);
        try {
            networkToSQL.run();
        } catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException | IOException e) {
            e.printStackTrace();
        }
    }
}
