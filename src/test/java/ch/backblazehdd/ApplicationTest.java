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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ApplicationTest extends AbstractBenchmark {


    File[] files;

    @Before
    public void setUp() throws Exception {
        File folder = new File("/Users/simon/Documents/dev/backblaze-hdd-stats/data");
        files = folder.listFiles();
    }


    // round: 1.66 [+- 0.06]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void readFileWithScanner() throws Exception {


        int i = 0;

        for (File file : files) {
            i += countLinesInFile(file);
        }

        assertEquals(5091501, i);

    }

    /**
     * round: 6.54 [+- 0.35]
     *
     * @throws Exception
     */
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void readFileWithCSVParser() throws Exception {

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
    public void readFileWithCSVFastAllAtOnce() throws Exception {

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

    /**
     * round: 0.54 [+- 0.04]
     *
     * @throws Exception
     */
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void parallelStreamRead() throws Exception {

        int i = Stream.of(files).parallel().map(this::countLinesInFile).mapToInt(p -> p).sum();

        assertEquals(5091501, i);

    }

    /**
     * round: 0.41 [+- 0.02]
     */
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void parallelStreamMoreThreads() {


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

    // round: 3.29 [+- 1.23]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void parallelStreamReadCSV() throws Exception {


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

    //round: 1.66 [+- 0.06]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void parallelCustomThreading() throws Exception {

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

    // round: 0.10 [+- 0.02]
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
    @Test
    public void splitLine() {
        String line = "2013-04-10,MJ0351YNG9Z0XA,Hitachi HDS5C3030ALA630,3000592982016,0,,0,,,,,,,,0,,,,,,4031,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,26,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n";
        String[] splitted;
        for (int i = 0; i < 21196; i++) {

            splitted = line.split(",");
        }
    }

    // round: 0.00 [+- 0.00]
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
    @Test
    public void splitLineV2() {
        String line = "2013-04-10,MJ0351YNG9Z0XA,Hitachi HDS5C3030ALA630,3000592982016,0,,0,,,,,,,,0,,,,,,4031,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,26,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n";
        String[] splitted;
        for (int i = 0; i < 21196; i++) {

            assertEquals("MJ0351YNG9Z0XA", line.substring(11, line.indexOf(44, 12)));
        }
    }

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


}
