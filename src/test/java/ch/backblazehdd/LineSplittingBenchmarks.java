package ch.backblazehdd;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LineSplittingBenchmarks extends AbstractBenchmark {


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

}
