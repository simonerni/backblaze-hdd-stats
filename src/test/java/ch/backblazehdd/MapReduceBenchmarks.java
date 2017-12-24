package ch.backblazehdd;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.reducing;
import static org.junit.Assert.assertEquals;

public class MapReduceBenchmarks extends AbstractBenchmark {

    File[] files;

    @Before
    public void setUp() throws Exception {
        File folder = new File("data/");
        files = folder.listFiles();
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
                                HardDrive::getID,
                                ConcurrentSkipListMap::new,
                                reducing(
                                        new HardDrive(),
                                        HardDrive::new,
                                        HardDrive::new)
                        ));


        assertEquals(true, concurrentMap.get("S1F032G7").isDead());

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
                                            HardDrive::getID,
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
