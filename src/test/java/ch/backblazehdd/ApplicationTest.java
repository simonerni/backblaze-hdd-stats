package ch.backblazehdd;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ApplicationTest {


    File[] files;

    @Before
    public void setUp() throws Exception {
        File folder = new File("/Users/simon/Documents/dev/backblaze-hdd-stats/data");
        files = folder.listFiles();
    }


    @Test
    public void parallelStreamRead() throws Exception {
        long time = System.currentTimeMillis();

        int sum = Stream.of(files).parallel().map(this::countLinesInFile).mapToInt(p -> p).sum();

        System.out.println("Parallel: " + (System.currentTimeMillis() - time));
        System.out.println("Read lines: " + sum);
    }

    @Test
    public void readFileWithScanner() throws Exception {


        int i = 0;

        long time = System.currentTimeMillis();

        for (File file : files) {
            i+= countLinesInFile(file);
        }

        System.out.println("Scanner: " + (System.currentTimeMillis() - time));
        System.out.println("Read lines: " + i);

    }

    @Test
    public void readFileWithCSVParser(File[] files) throws Exception {

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


    @Test
    public void readFileWithCSVFastAllAtOnce(File[] files) throws Exception {

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



    @Test
    public void testStream() {


        final int parallelism = 10;

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


    private int countLinesInFile(File file) {
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
