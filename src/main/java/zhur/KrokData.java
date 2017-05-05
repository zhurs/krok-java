package zhur;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

public class KrokData {
    public final int PAGES_NUM = 3;
    public final char[] LETTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};
    public final int[] NUMS = {1, 2, 3, 4, 5, 6, 7, 8};
    private final Random rnd;

    public int number;
    public int seed;
    public String season;
    public int year;
    public List<Map<String, String>> pages;
    public List<List<String>> chain;

    public KrokData(int number, int seed) {
        this.number = number;
        this.seed = seed;

        this.rnd = new Random(seed);

        this.season = KrokUtil.getSeason(number);
        this.year = KrokUtil.getYear(this.number);
        generateCodePages();
    }

    private String intToCode(int n) {
        return String.valueOf(LETTERS[n / NUMS.length]) + NUMS[n % NUMS.length];
    }

    public String getCode(int page, char letter, int num) {
        return pages.get(page).get(String.valueOf(letter) + num);
    }

    private void generateCodePages() {
        pages = new ArrayList<>(PAGES_NUM + 1);
        List<String> orderedCodes =
                IntStreamEx.range(LETTERS.length * NUMS.length)
                        .mapToObj(this::intToCode)
                        .toList();
        List<String> shuffledCodes = new ArrayList<>(orderedCodes);

        for (int i = 0; i < PAGES_NUM + 1; i++) {
            pages.add(StreamEx.of(orderedCodes).zipWith(shuffledCodes.stream()).toMap());
            Collections.shuffle(shuffledCodes, this.rnd);
        }

        chain = new ArrayList<>();
        for (String current : orderedCodes) {
            List<String> chainItem = new ArrayList<>();
            for (int i = 0; i < PAGES_NUM+1; i++) {
                current = pages.get(i).get(current);
                chainItem.add(current);
            }
            chain.add(chainItem);
        }
    }
}
