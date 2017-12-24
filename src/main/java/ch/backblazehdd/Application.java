package ch.backblazehdd;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.reducing;

public class Application {


    public static void main(String[] args) throws Exception {

        Application application = new Application();
        if (args.length != 0) {
            application.updateManufacturers();
        } else {
            application.run();
        }


    }

    private ConcurrentMap<String, HardDrive> calculateFromFiles() {
        final int parallelism = 8;

        File folder = new File("data/");
        File[] files = folder.listFiles();

        ForkJoinPool forkJoinPool = null;

        try {
            forkJoinPool = new ForkJoinPool(parallelism);

            return forkJoinPool.submit(() ->

                    //parallel stream invoked here
                    Stream
                            .of(files)
                            .parallel()
                            .flatMap(this::getStreamOfLines)
                            .collect(
                                    Collectors.groupingByConcurrent(
                                            HardDrive::getID,
                                            ConcurrentSkipListMap::new,
                                            reducing(
                                                    new HardDrive(), // Initial Element
                                                    HardDrive::new,  // Mapping function
                                                    HardDrive::new)  // "Merging" function
                                    ))


            ).get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            if (forkJoinPool != null) {
                forkJoinPool.shutdown(); //always remember to shutdown the pool
            }
        }

        return null;
    }

    private void outputResultCSV(Map<String, HardDrive> map) throws Exception {
        ModelMapper.initKnownModels(new File("mapping.csv"));

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data.csv")));

        out.println("life,death,model,manufacturer");

        for (HardDrive hardDrive : map.values()) {
            out.println(hardDrive.getCSVLine());
        }
    }

    protected void updateManufacturers() throws Exception {

        ModelMapper.initKnownModels(new File("mapping.csv"));

        List<String> strings = Files.lines(Paths.get("data.csv")).skip(1).map(line -> {

            String[] data = line.split(",");
            Model model = ModelMapper.getModelAndManufacturerFromModel(data[2]);

            if (!model.manufacturer.equals("")) {
                return data[0] + "," + data[1] + "," + model.model + "," + model.manufacturer;
            } else {
                return line;
            }

        }).collect(Collectors.toList());

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data.csv")));

        out.println("life,death,model,manufacturer");

        for (String line : strings) {
            out.println(line);
        }

    }

    protected void run() throws Exception {

        outputResultCSV(calculateFromFiles());

    }

    /**
     * Returns a stream of lines, without the first line.
     *
     * @param file The file to read.
     * @return A stream of strings, which are lines. When the file is not readable, returns an empty stream.
     */
    private Stream<String> getStreamOfLines(File file) {

        try {
            return Files.lines(Paths.get(file.toURI())).skip(1);
        } catch (IOException e) {
            e.printStackTrace();
            return Stream.ofNullable(null);
        }

    }

}
