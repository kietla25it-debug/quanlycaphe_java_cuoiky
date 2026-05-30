package auroracafe.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormatUtils {
    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(VI_LOCALE);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DecimalFormat HOURS = new DecimalFormat("0.00");

    private FormatUtils() {}

    public static String money(double value) {
        return MONEY.format(value);
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME.format(value);
    }

    public static String date(LocalDate value) {
        return value == null ? "-" : DATE.format(value);
    }

    public static String time(LocalDateTime value) {
        return value == null ? "-" : TIME.format(value);
    }

    public static String hours(double value) {
        return HOURS.format(value) + " giờ";
    }

    public static String workedTime(long minutes) {
        long h = Math.max(0, minutes) / 60;
        long m = Math.max(0, minutes) % 60;
        return h + " giờ " + String.format("%02d", m) + " phút";
    }

    public static String fileStamp(LocalDateTime value) {
        return FILE_STAMP.format(value);
    }
}
