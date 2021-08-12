package au.id.simo.useful.collections;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A Collection that sets a time-to-live for added items and provides a way to
 * remove them when their time-to-live expires.
 * <P>
 * Useful for tracking duplicate items over a specified time window.
 *
 * @param <E> The item type it contains
 */
public class ExpireCollection<E> extends AbstractCollection<E> {

    /**
     * Used in synchronised blocks when the array lists are modified.
     */
    private final Object syncLock = new Object();
    private final ArrayList<E> valueList;
    private final ArrayList<Instant> expiryList;
    private final Clock clock;
    private final Duration defaultExpiry;

    public ExpireCollection(Duration defaultExpiry) {
        this(Clock.systemDefaultZone(), defaultExpiry);
    }

    public ExpireCollection(Clock clock, Duration defaultExpiry) {
        this.clock = clock;
        this.defaultExpiry = defaultExpiry;
        valueList = new ArrayList<>();
        expiryList = new ArrayList<>();
    }

    @Override
    public boolean add(E e) {
        return add(e, defaultExpiry);
    }

    public boolean add(E e, Duration expiresIn) {
        synchronized (syncLock) {
            valueList.add(e);
            expiryList.add(clock.instant().plus(expiresIn));
        }
        return true;
    }

    public Instant getExpiry(int index) {
        return expiryList.get(index);
    }

    public List<E> removeExpired() {
        return removeExpired(clock.instant());
    }

    public List<E> removeExpired(Instant now) {
        List<E> expiredList = new ArrayList<>();
        for (int i = 0; i < expiryList.size(); i++) {
            if (now.isAfter(expiryList.get(i))) {
                synchronized (syncLock) {
                    expiryList.remove(i);
                    expiredList.add(valueList.remove(i));
                }
            }
        }
        return expiredList;
    }

    @Override
    public Iterator<E> iterator() {
        return new ExpireIterator();
    }

    public Iterator<ExpireEntry<E>> expiryIterator() {
        List<ExpireEntry<E>> list = new ArrayList<>(size());
        for (int i = 0; i < size(); i++) {
            list.add(new ExpireEntry<>(
                    expiryList.get(i),
                    valueList.get(i)
            ));
        }
        return list.iterator();
    }

    @Override
    public int size() {
        return valueList.size();
    }

    public static class ExpireEntry<T> {

        private final Instant expiry;
        private final T value;

        public ExpireEntry(Instant expiry, T value) {
            this.expiry = expiry;
            this.value = value;
        }

        public Instant getExpiry() {
            return expiry;
        }

        public T getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + Objects.hashCode(this.expiry);
            hash = 83 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ExpireEntry<?> other = (ExpireEntry<?>) obj;
            if (!Objects.equals(this.expiry, other.expiry)) {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }
    }

    private class ExpireIterator implements Iterator<E> {

        private int nextIndex = 0;

        @Override
        public boolean hasNext() {
            return valueList.size() > nextIndex;
        }

        @Override
        public E next() {
            if (nextIndex >= valueList.size()) {
                throw new NoSuchElementException("No more elements");
            }
            return valueList.get(nextIndex++);
        }

        @Override
        public void remove() {
            if (nextIndex <= 0) {
                throw new IllegalStateException("next() has yet to be called.");
            }
            int removeIndex = nextIndex - 1;
            synchronized (syncLock) {
                valueList.remove(removeIndex);
                expiryList.remove(removeIndex);
            }
        }
    }
}
