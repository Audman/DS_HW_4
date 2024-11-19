import java.util.ArrayList;
import java.util.Comparator;

import java.util.Iterator;
import java.util.NoSuchElementException;

interface Position<E> {
    E getElement() throws IllegalStateException;
}

interface PositionalList<E> extends Iterable<E> {

    int size();

    boolean isEmpty();

    Position<E> first();

    Position<E> last();

    Position<E> before(Position<E> p) throws IllegalArgumentException;

    Position<E> after(Position<E> p) throws IllegalArgumentException;

    Position<E> addFirst(E e);

    Position<E> addLast(E e);

    Position<E> addBefore(Position<E> p, E e)
        throws IllegalArgumentException;

    Position<E> addAfter(Position<E> p, E e)
        throws IllegalArgumentException;

    E set(Position<E> p, E e) throws IllegalArgumentException;

    E remove(Position<E> p) throws IllegalArgumentException;

    Iterator<E> iterator();

    Iterable<Position<E>> positions();
}

interface Stack<E> {
    int size();
    boolean isEmpty();
    void push(E e);
    E top();
    E pop();
}

interface PriorityQueue<K, V> {
    int size();
    boolean isEmpty();
    Entry<K, V> insert(K key, V value) throws IllegalArgumentException;
    Entry<K, V> min();
    Entry<K, V> removeMin();
}

interface Entry<K, V> {
    K getKey();
    V getValue();
}

class DefaultComparator<E> implements Comparator<E> {
    @SuppressWarnings({"unchecked"})
    public int compare(E a, E b) throws ClassCastException {
        return ((Comparable<E>) a).compareTo(b);
    }
}

abstract class AbstractPriorityQueue<K, V> implements PriorityQueue<K, V> {
    protected static class PQEntry<K, V> implements Entry<K, V> {
        private K k;
        private V v;
        public PQEntry(K key, V value) {
            k = key;
            v = value;
        }
        public K getKey() { return k; }
        public V getValue() { return v; }
        protected void setKey(K key) { k = key; }
        protected void setValue(V value) { v = value; }
    }

    private Comparator<K> comp;

    protected AbstractPriorityQueue(Comparator<K> c) { comp = c; }

    protected AbstractPriorityQueue() {
        this(new DefaultComparator<K>());
    }

    protected int compare(Entry<K, V> a, Entry<K, V> b) {
        return comp.compare(a.getKey(), b.getKey());
    }

    protected boolean checkKey(K key) throws IllegalArgumentException {
        try {
            return (comp.compare(key, key) == 0);
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("Incompatible key");
        }
    }

    public boolean isEmpty() { return size() == 0; }

}

class HeapPriorityQueue<K, V> extends AbstractPriorityQueue<K, V> {

    protected ArrayList<Entry<K, V>> heap = new ArrayList<>();

    public HeapPriorityQueue() {
        super();
    }

    public HeapPriorityQueue(Comparator<K> comp) {
        super(comp);
    }

    public HeapPriorityQueue(K[] keys, V[] values) {
        super();
        for (int j = 0; j < Math.min(keys.length, values.length); j++)
            heap.add(new PQEntry<>(keys[j], values[j]));
        heapify();
    }

    protected int parent(int j) {
        return (j - 1) / 2;
    }

    protected int left(int j) {
        return 2 * j + 1;
    }

    protected int right(int j) {
        return 2 * j + 2;
    }

    protected boolean hasLeft(int j) {
        return left(j) < heap.size();
    }

    protected boolean hasRight(int j) {
        return right(j) < heap.size();
    }

    protected void swap(int i, int j) {
        Entry<K, V> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    protected void upHeap(int j) {
        while (j > 0) { // continue until reaching root (or break statement)
            int p = parent(j);
            if (compare(heap.get(j), heap.get(p)) >= 0)
                break; // heap property verified
            swap(j, p);
            j = p; // continue from the parent's location
        }
    }

    protected void downHeap(int j) {
        while (hasLeft(j)) {
            int leftIndex = left(j);
            int smallChildIndex = leftIndex;
            if (hasRight(j)) {
                int rightIndex = right(j);
                if (compare(heap.get(leftIndex), heap.get(rightIndex)) > 0)
                    smallChildIndex = rightIndex;
            }
            if (compare(heap.get(smallChildIndex), heap.get(j)) >= 0)
                break;
            swap(j, smallChildIndex);
            j = smallChildIndex;
        }
    }

    protected void downheap(int j) {
        if (!hasLeft(j))
            return;

        int leftIndex = left(j);
        int small = leftIndex;

        if (hasRight(j)) {
            int rightIndex = right(j);
            if (compare(heap.get(leftIndex), heap.get(rightIndex)) > 0)
                small = rightIndex;
        }
        if (compare(heap.get(small), heap.get(j)) >= 0)
            return;
        swap(j, small);
        j = small;

        downheap(j);
    }

    protected void heapify() {
        int startIndex = parent(size() - 1); // start at PARENT of last entry
        for (int j = startIndex; j >= 0; j--) // loop until processing the root
            downHeap(j);
    }

    public int size() {
        return heap.size();
    }

    public Entry<K, V> min() {
        if (heap.isEmpty())
            return null;
        return heap.get(0);
    }

    public Entry<K, V> insert(K key, V value) throws IllegalArgumentException {
        checkKey(key); // auxiliary key-checking method (could throw exception)
        Entry<K, V> newest = new PQEntry<>(key, value);

        heap.add(newest); // add to the end of the list
        upHeap(heap.size() - 1); // upheap newly added entry
        return newest;
    }

    public Entry<K, V> removeMin() {
        if (heap.isEmpty())
            return null;
        Entry<K, V> answer = heap.get(0);
        swap(0, heap.size() - 1); // put minimum item at the end
        heap.remove(heap.size() - 1); // and remove it from the list;
        downHeap(0); // then fix new root
        return answer;
    }

    private void sanityCheck() {
        for (int j = 0; j < heap.size(); j++) {
            int left = left(j);
            int right = right(j);
            if (left < heap.size() && compare(heap.get(left), heap.get(j)) < 0)
                System.out.println("Invalid left child relationship");
            if (right < heap.size() && compare(heap.get(right), heap.get(j)) < 0)
                System.out.println("Invalid right child relationship");
        }
    }
}

public class PriorityQueueStack<E> implements Stack<E>
{
    HeapPriorityQueue<Integer, E> pq;

    public PriorityQueueStack() { pq = new HeapPriorityQueue<>(); }

    // O(1), it returns an integer
    public int size() { return pq.size(); }

    // O(1), it returns a boolean
    public boolean isEmpty() { return pq.isEmpty(); }

    // Adds an entry with the highest priority on the moment of adding, negative size is an easy way
    // O(log n), it can do up to log(n) upheaps
    public void push(E e) { pq.insert(-pq.size(), e); }

    // O(1), it just returns the root
    public E top() { return pq.min().getValue(); }

    // O(logn), it can do up to log(n) downheaps
    public E pop() { return pq.removeMin().getValue(); }

    public static void main(String[] args)
    {
        PriorityQueueStack<Character> stack = new PriorityQueueStack<>();

        for (int i = 0; i < 20; i++)
            stack.push((char)('a'+i));

        while(!stack.isEmpty())
            System.out.println(stack.pop());

        // As a professional unprofessional I can say, it works!
    }
}
