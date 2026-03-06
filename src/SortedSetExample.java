import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

public class SortedSetExample<T extends Comparable<T>> implements SortedSet<T> {

    class SortedSetExampleIterator<T extends Comparable<T>> implements Iterator<T> {

        @Override
        public boolean hasNext() {
            // TODO Auto-generated method stub
            return false;
        }
    
        @Override
        public T next() {
            // TODO Auto-generated method stub
            return null;
        }
    
    }
    
    
    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean add(T e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public Comparator<? super T> comparator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T first() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T last() {
        // TODO Auto-generated method stub
        return null;
    }

}