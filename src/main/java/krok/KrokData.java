package krok;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KrokData {
    private final String season;
    private final int year;
    private final List<Map<String, String>> pages;
    private final List<List<String>> chain;

    public String getCode(int page, char letter, int num) {
        return pages.get(page).get(String.valueOf(letter) + num);
    }
}
