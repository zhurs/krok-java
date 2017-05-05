package zhur;

import java.time.LocalDateTime;

public class KrokUtil {

    private static final int FIRST_KROK_YEAR = 1996;

    public static int getDefaultKrokNumber() {
        LocalDateTime now = LocalDateTime.now();
        int shift = now.getMonthValue() > 10 || now.getMonthValue() < 6
                ? 1
                : 2;
        return 2 * (now.getYear() - FIRST_KROK_YEAR) + shift;
    }

    public static int getYear(int krokNumber) {
        int shift = krokNumber % 2 == 1 ? 1 : 2;
        return FIRST_KROK_YEAR + (krokNumber - shift) / 2;
    }

    public static String getSeason(int number) {
        return number % 2 == 1 ? "май" : "осень";
    }
}
