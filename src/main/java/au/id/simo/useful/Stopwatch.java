package au.id.simo.useful;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Useful for tracking elapsed time and printing results in human readable
 * format.
 * <p>
 * The timer starts on instance construction and each call to any of the results
 * methods calculates the results at the time of the call. That is to say, the
 * start time is the only state contained in this class.
 */
public class Stopwatch {
    public static Stopwatch start() {
        return new Stopwatch();
    }
    
    private final Long startTime;

    public Stopwatch() {
        this(System.currentTimeMillis());
    }
    
    public Stopwatch(long startTime) {
        this.startTime = startTime;
    }

    public Long result() {
        return System.currentTimeMillis() - startTime;
    }

    public Double resultSec() {
        Double secs = result() / 1000d;
        BigDecimal bd = new BigDecimal(secs);
        BigDecimal rounded = bd.setScale(3, RoundingMode.HALF_UP);
        return rounded.doubleValue();
    }

    public String resultHuman() {
        Double secs = resultSec();
        int mins = 0;
        int hours = 0;
        int days = 0;
        
        while (secs > 60) {
            mins++;
            secs = secs - 60;
        }
        
        while(mins > 60) {
            hours++;
            mins = mins - 60;
        }
        
        while(hours > 24) {
            days++;
            hours = hours - 24;
        }

        // round secs to 3 decimal places
        BigDecimal bd = new BigDecimal(secs);
        BigDecimal roundedSec = bd.setScale(3, RoundingMode.HALF_UP);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days);
            sb.append(" days, ");
        }
        if (hours > 0) {
            sb.append(hours);
            sb.append(" hours, ");
        }
        if (mins > 0) {
            sb.append(mins);
            sb.append(" mins, ");
        }
        sb.append(roundedSec.doubleValue());
        sb.append(" secs");

        return sb.toString();
    }
}
