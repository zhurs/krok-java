package krok;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import static krok.KrokUtil.LETTERS;
import static krok.KrokUtil.NUMS;

public class KrokGenerator {

    private final int pagesNum;
    private final Random rnd;
    private final int number;


    public KrokGenerator(int pagesNum, int number, int seed) {
        this.pagesNum = pagesNum;
        this.rnd = new Random(seed);
        this.number = number;
    }

    public KrokData generate() {
        KrokData krok = new KrokData(KrokUtil.getSeason(number), KrokUtil.getYear(this.number));
        generateCodePages(krok);
        return krok;
    }

    private void generateCodePages(KrokData krok) {
        List<Map<String, String>> pages = new ArrayList<>(pagesNum + 1);
        List<String> orderedCodes =
                IntStreamEx.range(LETTERS.length * NUMS.length)
                        .mapToObj(this::intToCode)
                        .toList();
        List<String> shuffledCodes = new ArrayList<>(orderedCodes);

        for (int i = 0; i < pagesNum + 1; i++) {
            pages.add(StreamEx.of(orderedCodes).zipWith(shuffledCodes.stream()).toMap());
            Collections.shuffle(shuffledCodes, this.rnd);
        }

        List<List<String>> chain = new ArrayList<>();
        for (String current : orderedCodes) {
            List<String> chainItem = new ArrayList<>();
            for (int i = 0; i < pagesNum +1; i++) {
                current = pages.get(i).get(current);
                chainItem.add(current);
            }
            chain.add(chainItem);
        }

        krok.withPages(pages)
                .withChain(chain);
    }

    private String intToCode(int n) {
        return String.valueOf(LETTERS[n / NUMS.length]) + NUMS[n % NUMS.length];
    }


}
