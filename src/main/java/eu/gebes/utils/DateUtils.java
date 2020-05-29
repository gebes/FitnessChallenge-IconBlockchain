package eu.gebes.utils;

import javax.swing.text.DateFormatter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String convertUnixSecondsToDate(BigInteger unix){
        // Java erwartet Millisekunden anstelle von Sekunden
        var date = new Date(unix.longValue()* 1000L);
        var dateFormatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        return dateFormatter.format(date);
    }


}
