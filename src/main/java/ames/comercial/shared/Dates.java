package ames.comercial.shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class Dates {
    public static SimpleDateFormat formatter_simple = new SimpleDateFormat("dd/MM/yyyy");
    public static SimpleDateFormat formatter_simple_EDI = new SimpleDateFormat("dd/MM/yyyy");
    public static DateTimeFormatter formatter_datetime = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static DateTimeFormatter formatter_datetime_EDI = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    static final Logger log = LogManager.getLogger(Dates.class.getName());

    public static String getCurrentDateDay() {
        if (LocalDate.now().getDayOfMonth() < 10)
            return "0" + LocalDate.now().getDayOfMonth();
        else
            return "" + LocalDate.now().getDayOfMonth();
    }

    public static String getCurrentDateMonth() {
        return getCurrentDateMonth(0);
    }

    public static String getCurrentDateMonth(int lag) {
        int monthIndex = LocalDate.now().getMonthValue() + lag;
        if (monthIndex < 10)
            return "0" + (monthIndex);
        else
            return "" + (monthIndex);
    }

    public static int getCurrentDateYear() {
        return getCurrentDateYear(0);
    }

    public static int getCurrentDateYear(int lag) {
        return LocalDate.now().getYear() + lag;
    }

    public static Date getNowUtilDate() {
        return getNowUtilDate(0);
    }

    public static java.sql.Date convertToSQLDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate);
    }

    public static Date convertToUtilDate(LocalDate localDate) {
        LocalDateTime localDateTime = localDate.atStartOfDay();
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date getNowUtilDate(int plusDays) {
        try {
            Date date = formatter_simple.parse(getNowStringDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, plusDays);
            return calendar.getTime();
        } catch (ParseException e) {
            log.error("Error al obtenir una data");
            return null;
        }
    }

    public static java.sql.Date getNowSQLDate() {
        return new java.sql.Date(getNowUtilDate().getTime());
    }

    public static java.sql.Date getNowSQLDate(int plusDays) {
        return new java.sql.Date(getNowUtilDate(plusDays).getTime());
    }

    public static String getNowStringDate() {
        return getCurrentDateDay() + "/" + getCurrentDateMonth() + "/" + getCurrentDateYear();
    }

    public static LocalDate getLocalDatePlusMonths(int numOfMonths) {
        return LocalDate.now().plus(numOfMonths, ChronoUnit.MONTHS);
    }

    public static LocalDate getLocalDatePlusDays(int numOfDays) {
        return LocalDate.now().plus(numOfDays, ChronoUnit.DAYS);
    }

    public static int compareEDIDates(String date1, String date2) {
        LocalDate fecha1 = LocalDate.parse(date1, formatter_datetime_EDI);
        LocalDate fecha2 = LocalDate.parse(date2, formatter_datetime_EDI);

        // Comparar fechas
        if (fecha1.isBefore(fecha2)) {
            return 1;
        } else if (fecha1.isAfter(fecha2)) {
            return -1;
        } else {
            return 0;
        }
    }

//    public static boolean isValidEDIDate(String date) {
//        return (!date.equals("") && !date.equals("/  /"));
//    }

    public static boolean isValidEDIDate(String dateStr) {
        // Expresión regular para validar el formato dd/MM/yyyy
        String regex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$";
        Pattern pattern = Pattern.compile(regex);

        // Primero verificamos si la fecha coincide con el patrón
        if (!pattern.matcher(dateStr).matches()) {
            return false;
        }

        // Ahora tratamos de parsear la fecha para verificar si es válida
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);

            // Si la fecha es válida, retornamos true
            return true;
        } catch (DateTimeParseException e) {
            // Si ocurre una excepción al parsear, la fecha no es válida
            return false;
        }
    }

//    public static LocalDate calcularDataMagatzem(LocalDate data,Long diesTransitClient) {
//        // Resta dels dies de transit
//        var resultat = data.minusDays(diesTransitClient);
//        // Normalització dels dies de sortida per a que de dilluns a divendres
//        // sigui (XXXXX··) i no (·XXXXX·) que es com està guardat a BBDD
//        // i també s'afegeixen els espais fins a omplir 7 caràcters
//        // Comprovar si la data es vàlida
//        var isValidDate = "XXXXX  ".charAt(resultat.getDayOfWeek().getValue()-1) != ' ';
//        while (!isValidDate) {
//            resultat = resultat.minusDays(1);
//            isValidDate = "XXXXX  ".charAt(resultat.getDayOfWeek().getValue()-1) != ' ';
//        }
//        return resultat;
//    }

    public static int setmana(LocalDate data) {
        return data.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public static void main(String[] args) {
        System.out.println("Avui és (String): " + getNowStringDate());
        System.out.println("Avui és (java.sql.Date): " + getNowSQLDate());
        System.out.println("Resultat comparació: " + compareEDIDates("03/01/2021", "01/01/2021"));
    }
}