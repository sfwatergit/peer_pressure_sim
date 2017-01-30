package sandbox.sfwatergit.analysis.scripts;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;

import static org.matsim.core.utils.io.IOUtils.getBufferedWriter;

/**
 * Created by sidneyfeygin on 12/4/15.
 */
public class FluentTableWriter {

    public static final String HLINE = "\\\\ \\hline";
    public static final String SEPARATOR = " & ";
    Logger log = Logger.getLogger(FluentTableWriter.class);


    private LinkedList<String[]> data;
    private boolean latex = false;
    private int n;

    // non-instantiable from routeCaching
    private FluentTableWriter() {
    }

    public static void write(final Consumer<FluentTableWriter> writer) {
        final FluentTableWriter fluentTableWriter = new FluentTableWriter();
        writer.accept(fluentTableWriter);
    }

    public static void main(String[] args) {
        FluentTableWriter.write(writer ->
                writer
                        .header(new String[]{"a vals", "b vals"})
                        .row(new String[]{"1", "2"})
                        .row(new String[]{"2", "3"})
                        .toLatex(true)
                        .toFile("ok.txt"));
    }

    public FluentTableWriter header(final String[] header) {
        data = Lists.newLinkedList();
        n = header.length;
        data.add(header);
        return this;
    }

    public FluentTableWriter row(final String[] row) {
        data.add(row);
        return this;
    }

    public FluentTableWriter toLatex(boolean firstCol) {
        final StringBuilder m = new StringBuilder("*{");
        m.append(String.valueOf(n - 1));
        m.append("}{c}}");

        StringBuilder b = new StringBuilder()
                .append("\\begin{tabular}{")
                .append((firstCol ? " l " : " c "))
                .append(m.toString());

        LinkedList<String[]> temp = Lists.newLinkedList();
        temp.add(new String[]{b.toString()});
        temp.add(new String[]{Joiner.on(SEPARATOR).join(data.getFirst()).concat(HLINE)});
        temp.forEach(d -> temp.add(new String[]{Joiner.on(SEPARATOR).join(d)}));
        temp.addLast(new String[]{"\\end{tabular}"});
        data = temp;

        return this;

    }

    public FluentTableWriter toFile(String filename) {
        final BufferedWriter bufferedWriter = getBufferedWriter(filename);
        log.info("Writing...");
        data.forEach(row -> {
            try {
                if (!latex) {
                    bufferedWriter.write(Joiner.on('\t').join(row));
                } else {
                    bufferedWriter.write(row[0]);
                }
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        log.info("Done!");
        return this;
    }
}
