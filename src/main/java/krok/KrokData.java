package krok;

import java.util.List;
import java.util.Map;

public class KrokData {
    private String season;
    private int year;
    private List<Map<String, String>> pages;
    private List<List<String>> chain;

    public KrokData(String season, int year) {
        this.season = season;
        this.year = year;
    }

    public KrokData withPages(List<Map<String, String>> pages) {
        this.pages = pages;
        return this;
    }

    public KrokData withChain(List<List<String>> chain) {
        this.chain = chain;
        return this;
    }

    public String getSeason() {
        return season;
    }

    public int getYear() {
        return year;
    }

    public List<Map<String, String>> getPages() {
        return pages;
    }

    public List<List<String>> getChain() {
        return chain;
    }

    public String getCode(int page, char letter, int num) {
        return pages.get(page).get(String.valueOf(letter) + num);
    }
}
