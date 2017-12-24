package ch.backblazehdd;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.reducing;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
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
        for (int i = 0; i < 21196; i++) {

            assertEquals("MJ0351YNG9Z0XA", line.substring(11, line.indexOf(44, 12)));
        }
    }

    // round: 0.52 [+- 0.09]
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 1)
    @Test
    public void lineStreamTest() {

        long i = Stream.of(files).parallel().flatMap(this::getStreamOfLines).count();

        assertEquals(5091501L, i);


    }

    // round: 2.80 [+- 0.19]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void fullStreamTest() {


        ConcurrentMap<String, HardDrive> concurrentMap = Stream
                .of(files)
                .parallel()
                .flatMap(this::getStreamOfLines)
                .collect(
                        Collectors.groupingByConcurrent(
                                this::getIDFromHDD,
                                ConcurrentSkipListMap::new,
                                reducing(
                                        new HardDrive(),
                                        HardDrive::new,
                                        HardDrive::new)
                        ));


        System.out.println(concurrentMap.get("S1F032G7"));

    }

    // round: 1.78 [+- 0.18]
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 1)
    @Test
    public void fullStreamTestMoreThreads() {


        final int parallelism = 8;

        ForkJoinPool forkJoinPool = null;

        try {
            forkJoinPool = new ForkJoinPool(parallelism);

            ConcurrentMap<String, HardDrive> i = forkJoinPool.submit(() ->

                    //parallel stream invoked here
                    Stream
                            .of(files)
                            .parallel()
                            .flatMap(this::getStreamOfLines)
                            .collect(
                                    Collectors.groupingByConcurrent(
                                            this::getIDFromHDD,
                                            ConcurrentSkipListMap::new,
                                            reducing(
                                                    new HardDrive(), // Initial Element
                                                    HardDrive::new,  // Mapping function
                                                    HardDrive::new)  // "Merging" function
                                    ))


            ).get(); //this makes it an overall blocking call

            assertEquals(true, i.get("S1F032G7").isDead());


        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            if (forkJoinPool != null) {
                forkJoinPool.shutdown(); //always remember to shutdown the pool
            }
        }

    }


    @Test
    public void testGetIDFromHDD() {
        String line = "2013-04-10,6XW0SVS9,ST31500541AS,1500301910016,0,,87406718,,,,,,,,0,,,,,,28428,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,29,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n";

        String id = this.getIDFromHDD(line);

        assertEquals("ID", "6XW0SVS9", id);

    }

    @Test
    public void testGetID2FromHDD() {
        String line = "2013-04-10,WD-WCAU4A648671,WDC WD10EADS,1000204886016,0,,0,,,,,,,,0,,,,,,31475,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,22,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,";

        String id = this.getIDFromHDD(line);

        assertEquals("ID", "WD-WCAU4A648671", id);

    }


    public String getIDFromHDD(String line) {
        return line.substring(11, line.indexOf(44, 12));
    }

    @Test
    public void stringCompareEmpty() {

        HardDrive empty = new HardDrive();

        HardDrive date1 = new HardDrive("2013-04-10", "2013-04-10");


        HardDrive merged = new HardDrive(empty, date1);
        assertEquals(date1.getMax(), merged.getMax());
        assertEquals(date1.getMin(), merged.getMin());

    }

    @Test
    public void stringCompareNewMin() {


        HardDrive date1 = new HardDrive("2013-04-10", "2013-04-10");

        HardDrive date2 = new HardDrive("2011-04-10", "2013-04-10");


        HardDrive merged = new HardDrive(date1, date2);
        assertEquals(date1.getMax(), merged.getMax());
        assertEquals(date2.getMin(), merged.getMin());

    }

    @Test
    public void stringCompareNewMaxAndMin() {


        HardDrive date1 = new HardDrive("2013-04-10", "2015-04-10");

        HardDrive date2 = new HardDrive("2011-04-10", "2013-04-10");


        HardDrive merged = new HardDrive(date1, date2);
        assertEquals(date1.getMax(), merged.getMax());
        assertEquals(date2.getMin(), merged.getMin());

    }

    @Test
    public void mergedModel() {


        HardDrive empty = new HardDrive();

        HardDrive hdd = new HardDrive("2013-04-10,9VS3FM1J,ST31500341AS,1500301910016,1,,222508045,,,,,,,,4094,,,,,,26993,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,31,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n");


        HardDrive merged = new HardDrive(empty, hdd);
        assertEquals("Max", hdd.getMax(), merged.getMax());
        assertEquals("Min", hdd.getMin(), merged.getMin());
        assertEquals("Model", hdd.getModel(), merged.getModel());
        assertEquals("Death", hdd.isDead(), merged.isDead());

    }

    @Test
    public void testHardDriveLineParse() {

        String line = "2013-04-10,MJ0351YNG9Z7LA,Hitachi HDS5C3030ALA630,3000592982016,0,,0,,,,,,,,0,,,,,,3593,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,26,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n";

        HardDrive hardDrive = new HardDrive(line);
        assertEquals("Minimal Date", "2013-04-10", hardDrive.getMin());
        assertEquals("Maximal Date", "2013-04-10", hardDrive.getMax());

        assertEquals("Model", "Hitachi HDS5C3030ALA630", hardDrive.getModel());

        assertFalse("Dead", hardDrive.isDead());

    }

    @Test
    public void testHardDriveLineParseDeath() {

        String line = "2013-04-10,9VS3FM1J,ST31500341AS,1500301910016,1,,222508045,,,,,,,,4094,,,,,,26993,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,31,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n";

        HardDrive hardDrive = new HardDrive(line);
        assertEquals("Minimal Date", "2013-04-10", hardDrive.getMin());
        assertEquals("Maximal Date", "2013-04-10", hardDrive.getMax());

        assertEquals("Model", "ST31500341AS", hardDrive.getModel());

        assertTrue("Dead", hardDrive.isDead());

    }



    /**
     * This method has been too slow, that's why I discarded it.
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


    /**
     * Returns a stream of lines, without the first line.
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


}
