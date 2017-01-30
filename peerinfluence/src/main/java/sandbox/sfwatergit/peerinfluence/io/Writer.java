package sandbox.sfwatergit.peerinfluence.io;

import java.io.*;

public class Writer {

    BufferedWriter writer;

    public void createFile(String path) {
        try {
            File file = new File(path);
            this.writer = new BufferedWriter(new FileWriter(file));
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("something messed up");
            System.exit(1);
        }
    }


    public void writeLine(String data) {

        try {
            writer.write(data);
            writer.newLine();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("something messed up");
            System.exit(1);
        }
    }


    public void close() {
        try {
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("something messed up");
            System.exit(1);
        }
    }
}