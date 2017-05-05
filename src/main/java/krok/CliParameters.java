package krok;

import java.util.concurrent.ThreadLocalRandom;

import com.beust.jcommander.Parameter;

public class CliParameters {
    @Parameter(names = "-O", description = "Output file")
    public String outputFile = "krok-codepages.pdf";

    @Parameter(names = "-n", description = "Krok number")
    public int number = 0;

    @Parameter(names = "--pages", description = "Pages count")
    public int pages = 20;

    @Parameter(names = "--seed", description = "Random seed")
    public int seed = ThreadLocalRandom.current().nextInt(1_000_000);

    public CliParameters() {
        this.number = KrokUtil.getDefaultKrokNumber();
    }
}
