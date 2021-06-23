package au.id.simo.useful;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Convenience class for keeping count of events from within an application.
 * <p>
 * Within your application event code, add {@code Counts.increment("hits")}.
 * Then your reporting code could obtain totals via
 * {@code long hits = Counts.get("hits");}
 */
public class Counts {

    private static final Map<String, AtomicLong> countsMap = new ConcurrentHashMap<>();

    private Counts() {
        // no-op
    }

    /**
     * Adds one to the named count. If the named count doesn't exist then it
     * will be first be created with a value of 0 and then incremented to 1.
     *
     * @param countName the name of the count to increase by one.
     * @return the newly incremented value.
     */
    public static long increment(String countName) {
        AtomicLong al = countsMap.computeIfAbsent(
                countName,
                key -> new AtomicLong()
        );
        return al.incrementAndGet();
    }

    /**
     * Set the specified value of the named count. If the named count didn't
     * previously exist, it will be created.
     *
     * @param countName the named count to set.
     * @param value the value to set the named count.
     * @return the replaced number, or 0 of there was no previous value.
     */
    public static long set(String countName, long value) {
        AtomicLong al = countsMap.put(countName, new AtomicLong(value));
        if (al != null) {
            return al.longValue();
        } else {
            return 0l;
        }
    }

    /**
     * Returns the count value of the given named count.
     *
     * @param countName the name of the count to obtain the value of.
     * @return the current value of the named count. If the count doesn't exist
     * then 0 is returned.
     */
    public static long get(String countName) {
        AtomicLong al = countsMap.get(countName);
        if (al == null) {
            return 0l;
        } else {
            return al.longValue();
        }
    }

    /**
     * Lists the name of all existing counts.
     *
     * @return A list containing a copy of all existing count names at time of
     * method call. Any changes to the returned list will not affect the counts
     * managed within this class.
     */
    public static List<String> getCountNames() {
        Set<String> keySet = countsMap.keySet();
        return new ArrayList<>(keySet);
    }

    /**
     * Removes the named count.
     *
     * @param countName the name of the count to remove.
     * @return Any value that was present at time of removal. Otherwise 0 if
     * count doesn't exist.
     */
    public static long removeCount(String countName) {
        AtomicLong atomicLong = countsMap.remove(countName);
        if (atomicLong == null) {
            return 0l;
        }
        return atomicLong.get();
    }

    /**
     * Removes all counts.
     */
    public static void removeAll() {
        countsMap.clear();
    }
}
