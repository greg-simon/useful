package au.id.simo.useful.test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;

/**
 *
 */
public class ManualClock extends Clock {

    private Instant instant;
    
    public ManualClock() {
        this.instant = Instant.EPOCH;
    }
    
    public ManualClock(Instant instant) {
        this.instant = instant;
    }
    
    public void setInstant(Instant instant) {
        this.instant = instant;
    }
    
    public void increment(TemporalAmount tempAmount) {
        this.instant = this.instant.plus(tempAmount);
    }
    
    @Override
    public Instant instant() {
        return instant;
    }
    
    @Override
    public ZoneId getZone() {
        return ZoneId.systemDefault();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
