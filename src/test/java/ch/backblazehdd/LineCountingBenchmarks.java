package ch.backblazehdd;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class LineCountingBenchmarks extends AbstractBenchmark {

    File[] files;

    @Before
    public void setUp() throws Exception {
        File folder = new File("data/");
        files = folder.listFiles();
    }

    // round: 1.66 [+- 0.06]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void singleThreadCountLines() throws Exception {


        int i = 0;

        for (File file : files) {
            i += countLinesInFile(file);
        }

        assertEquals(5091501, i);

    }

    // round: 6.54 [+- 0.35]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void singleThreadCountLinesWithCSVParser() throws Exception {

        int i = 0;

        CsvReader csvReader = new CsvReader();
        csvReader.setContainsHeader(true);

        for (File file : files) {
            try (CsvParser csvParser = csvReader.parse(file, StandardCharsets.UTF_8)) {
                CsvRow row;

                while ((row = csvParser.nextRow()) != null) {
                    i++;
                    row.getFieldCount();
                }


            }
        }

        assertEquals(5091501, i);


    }

    // round: 6.70 [+- 0.50]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void singleThreadCountLinesWithCSVParserAllAtOnce() throws Exception {

        int i = 0;

        CsvReader csvReader = new CsvReader();
        csvReader.setContainsHeader(true);

        for (File file : files) {
            CsvContainer csvParser = csvReader.read(file, StandardCharsets.UTF_8);

            for (CsvRow row : csvParser.getRows()) {
                i++;
                row.getFieldCount();
            }
        }
        assertEquals(5091501, i);

    }


    // round: 3.29 [+- 1.23]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void parallelStreamCSVReader() throws Exception {


        CsvReader csvReader = new CsvReader();
        csvReader.setContainsHeader(true);


        long i = Stream.of(files)
                .parallel()
                .flatMap((File file) -> {
                    try {
                        return csvReader.read(file, StandardCharsets.UTF_8).getRows().stream();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                })
                .count();

        assertEquals(5091501L, i);
    }

    /**
     * round: 0.54 [+- 0.04]
     *
     * @throws Exception
     */
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void parallelCountLinesStream() throws Exception {

        int i = Stream.of(files).parallel().map(this::countLinesInFile).mapToInt(p -> p).sum();

        assertEquals(5091501, i);

    }

    /**
     * round: 0.41 [+- 0.02]
     */
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void parallelCountLinesStreamCustomForkJoinPool() {


        final int parallelism = 16;

        ForkJoinPool forkJoinPool = null;

        try {
            forkJoinPool = new ForkJoinPool(parallelism);

            int i = forkJoinPool.submit(() ->

                    //parallel stream invoked here
                    Stream.of(files).parallel().map(this::countLinesInFile).mapToInt(p -> p).sum()


            ).get(); //this makes it an overall blocking call

            assertEquals(5091501, i);


        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            if (forkJoinPool != null) {
                forkJoinPool.shutdown(); //always remember to shutdown the pool
            }
        }


    }


    //round: 1.66 [+- 0.06]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void parallelCountLinesCustomThreading() throws Exception {

        int parallel = 100;

        ConcurrentLinkedQueue<File> fileQueue = new ConcurrentLinkedQueue<>();

        AtomicInteger atomicInteger = new AtomicInteger();

        fileQueue.addAll(Arrays.asList(files));

        class MyThread extends Thread {
            public void run() {

                File file = fileQueue.poll();
                while (file != null) {

                    atomicInteger.getAndAdd(countLinesInFile(file));

                    file = fileQueue.poll();

                }


            }
        }

        List<Thread> threadList = new ArrayList<>(parallel);
        for (int i = 0; i < parallel; i++) {
            Thread thread = new MyThread();
            threadList.add(thread);
            thread.run();
        }

        for (Thread thread : threadList) {
            thread.join();
        }

        assertEquals(5091501, atomicInteger.get());


    }

    /**
     * Currently fastest implementation of counting lines in a file without using streams.
     *
     * @param file The file to read in.
     * @return The count of lines, -1 (without the header in CSV files).
     */
    private int countLinesInFile(File file) {
        int i = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            //Skip First line (headers)
            reader.readLine();

            String text;

            do {
                text = reader.readLine();
                if (text != null) {
                    i++;
                }
            } while (text != null);

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return i;
    }


    /**
     * This method has been too slow, that's why I discarded it, but keeping here for future reference.
     *
     * @param file The file to read.
     * @return The number of lines (-1), skipping the first line.
     */
    private int countLinesInFileOld(File file) {
        int i = 0;

        try {
            Scanner scanner = new Scanner(file);

            scanner.nextLine();
            while (scanner.hasNextLine()) {
                i++;
                scanner.nextLine();
            }

            scanner.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        return i;

    }


}
