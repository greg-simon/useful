package au.id.simo.useful.collections;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Can group iterators together and iterator over all of them in order.
 */
public class ConcatIterator<E> implements Iterator<E> {

    private final Iterator<Iterator<E>> itrIterator;
    private Iterator<E> currentItr;

    @SafeVarargs
    public ConcatIterator(Iterator<E>... itrs) {
        this.itrIterator = Arrays.asList(itrs).iterator();
    }
    
    @Override
    public boolean hasNext() {
        while(currentItr == null || !currentItr.hasNext()) {
            if (itrIterator.hasNext()) {
                currentItr = itrIterator.next();
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public E next() {
        return currentItr.next();
    }
}
