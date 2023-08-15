package account.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static boolean isValidMMYYYYFormat(String inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        try {
            YearMonth yearMonth = YearMonth.parse(inputDate, formatter);
            return yearMonth.getMonthValue() >= 1 && yearMonth.getMonthValue() <= 12;
        } catch (DateTimeException e) {
            return false;
        }
    }

    public static String convertToNameOfMonthFormat(String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM-yyyy");
        YearMonth yearMonth = YearMonth.parse(inputDate, inputFormatter);
        LocalDate localDate = yearMonth.atDay(1);
        return localDate.format(outputFormatter);
    }
}
