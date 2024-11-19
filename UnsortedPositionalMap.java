import java.util.Iterator;
import java.util.NoSuchElementException;

interface Map<K,V>
{
    int size();
    boolean isEmpty();
    V get(K key);
    V put(K key, V value);
    V remove(K key);
    Iterable<K> keySet();
    Iterable<V> values();
    Iterable<Entry<K,V>> entrySet();
}

class LinkedPositionalList<E> implements PositionalList<E>
{
    private static class Node<E> implements Position<E>
    {
        private E element;
        private Node<E> prev;
        private Node<E> next;
        public Node(E e, Node<E> p, Node<E> n) {
            element = e;
            prev = p;
            next = n;
        }

        public E getElement() throws IllegalStateException {
            if (next == null)
                throw new IllegalStateException("Position no longer valid");
            return element;
        }

        public Node<E> getPrev() {
            return prev;
        }

        public Node<E> getNext() {
            return next;
        }

        public void setElement(E e) {
            element = e;
        }

        public void setPrev(Node<E> p) {
            prev = p;
        }

        public void setNext(Node<E> n) {
            next = n;
        }
    }

    private Node<E> header;

    private Node<E> trailer;

    private int size = 0;

    public LinkedPositionalList() {
        header = new Node<>(null, null, null);
        trailer = new Node<>(null, header, null);
        header.setNext(trailer);
    }

    private Node<E> validate(Position<E> p) throws IllegalArgumentException {
        if (!(p instanceof Node)) throw new IllegalArgumentException("Invalid p");
        Node<E> node = (Node<E>) p;
        if (node.getNext() == null)
            throw new IllegalArgumentException("p is no longer in the list");
        return node;
    }

    private Position<E> position(Node<E> node) {
        if (node == header || node == trailer)
            return null;
        return node;
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    public Position<E> first() {
        return position(header.getNext());
    }

    public Position<E> last() {
        return position(trailer.getPrev());
    }

    public Position<E> before(Position<E> p) throws IllegalArgumentException {
        Node<E> node = validate(p);
        return position(node.getPrev());
    }

    public Position<E> after(Position<E> p) throws IllegalArgumentException {
        Node<E> node = validate(p);
        return position(node.getNext());
    }

    private Position<E> addBetween(E e, Node<E> pred, Node<E> succ) {
        Node<E> newest = new Node<>(e, pred, succ);
        pred.setNext(newest);
        succ.setPrev(newest);
        size++;
        return newest;
    }

    public Position<E> addFirst(E e) {
        return addBetween(e, header, header.getNext());
    }

    public Position<E> addLast(E e) {
        return addBetween(e, trailer.getPrev(), trailer);
    }

    public Position<E> addBefore(Position<E> p, E e)
        throws IllegalArgumentException {
        Node<E> node = validate(p);
        return addBetween(e, node.getPrev(), node);
    }

    public Position<E> addAfter(Position<E> p, E e)
        throws IllegalArgumentException {
        Node<E> node = validate(p);
        return addBetween(e, node, node.getNext());
    }

    public E set(Position<E> p, E e) throws IllegalArgumentException {
        Node<E> node = validate(p);
        E answer = node.getElement();
        node.setElement(e);
        return answer;
    }

    public E remove(Position<E> p) throws IllegalArgumentException {
        Node<E> node = validate(p);
        Node<E> predecessor = node.getPrev();
        Node<E> successor = node.getNext();
        predecessor.setNext(successor);
        successor.setPrev(predecessor);
        size--;
        E answer = node.getElement();
        node.setElement(null);
        node.setNext(null);
        node.setPrev(null);
        return answer;
    }

    private class PositionIterator implements Iterator<Position<E>> {

        private Position<E> cursor = first();

        private Position<E> recent = null;

        public boolean hasNext() { return (cursor != null);  }

        public Position<E> next() throws NoSuchElementException {
            if (cursor == null) throw new NoSuchElementException("nothing left");
            recent = cursor;
            cursor = after(cursor);
            return recent;
        }

        public void remove() throws IllegalStateException {
            if (recent == null) throw new IllegalStateException("nothing to remove");
            LinkedPositionalList.this.remove(recent);
            recent = null;
        }
    }

    private class PositionIterable implements Iterable<Position<E>> {
        public Iterator<Position<E>> iterator() { return new PositionIterator(); }
    }

    public Iterable<Position<E>> positions() {
        return new PositionIterable();
    }

    private class ElementIterator implements Iterator<E> {
        Iterator<Position<E>> posIterator = new PositionIterator();
        public boolean hasNext() { return posIterator.hasNext(); }
        public E next() { return posIterator.next().getElement(); }
        public void remove() { posIterator.remove(); }
    }

    public Iterator<E> iterator() { return new ElementIterator(); }

    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        Node<E> walk = header.getNext();
        while (walk != trailer) {
            sb.append(walk.getElement());
            walk = walk.getNext();
            if (walk != trailer)
                sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}

abstract class AbstractMap<K,V> implements Map<K,V>
{
    public boolean isEmpty() { return size() == 0; }

    protected static class MapEntry<K,V> implements Entry<K,V> {
        private K k;
        private V v;

        public MapEntry(K key, V value) {
            k = key;
            v = value;
        }

        public K getKey() { return k; }
        public V getValue() { return v; }

        protected void setKey(K key) { k = key; }
        protected V setValue(V value) {
            V old = v;
            v = value;
            return old;
        }

        public String toString() { return "<" + k + ", " + v + ">"; }
    }

    private class KeyIterator implements Iterator<K> {
        private Iterator<Entry<K,V>> entries = entrySet().iterator();
        public boolean hasNext() { return entries.hasNext(); }
        public K next() { return entries.next().getKey(); }
        public void remove() { throw new UnsupportedOperationException("remove not supported"); }
    }

    private class KeyIterable implements Iterable<K> {
        public Iterator<K> iterator() { return new KeyIterator(); }
    }

    public Iterable<K> keySet() { return new KeyIterable(); }

    private class ValueIterator implements Iterator<V> {
        private Iterator<Entry<K,V>> entries = entrySet().iterator();
        public boolean hasNext() { return entries.hasNext(); }
        public V next() { return entries.next().getValue(); }
        public void remove() { throw new UnsupportedOperationException("remove not supported"); }
    }

    private class ValueIterable implements Iterable<V> {
        public Iterator<V> iterator() { return new ValueIterator(); }
    }

    public Iterable<V> values() { return new ValueIterable(); }
}

public class UnsortedPositionalMap<K,V> extends AbstractMap<K, V>
{
    LinkedPositionalList<Entry<K,V>> lpl;

    public UnsortedPositionalMap() { lpl = new LinkedPositionalList<>(); }

    public int size() { return lpl.size(); }

    public V get(K key)
    {
        Position<Entry<K,V>> pos = findKey(key);

        if (pos != null)
            return pos.getElement().getValue();

        return null;
    }

    public V put(K key, V value)
    {
        Position<Entry<K,V>> pos = findKey(key);
        V returnValue = null;

        if (pos != null)
            returnValue = lpl.remove(pos).getValue();

        lpl.addLast(new MapEntry<>(key, value));

        return returnValue;
    }

    public V remove(K key)
    {
        return lpl.remove(findKey(key)).getValue();
    }

    public Iterable<Entry<K,V>> entrySet()
    {
        // Ideally it should return a copy/clone of it, but it is what it is.
        //
        // It's memory efficient,
        // it's Iterable<Entry<K,V>>,
        // it works! (theoretically)
        return lpl;
    }

    public Position<Entry<K,V>> findKey(K key)
    {
        for (Position<Entry<K,V>> pos: lpl.positions())
            if(pos.getElement().getKey() == key)
                return pos;
        return null;
    }

    public static void main(String[] args) {
        LinkedHeapPriorityQueue<Integer,Integer> lhpq =
            new LinkedHeapPriorityQueue<>();

        for (int i = 0; i < 64; i++) {
            lhpq.insert(i,i);
        }

        int[] arr = new int[64];
        for (int i = 0; i < 64; i++) {
            arr[i] = lhpq.removeMin().getValue();
        }

        System.out.println(arr);
        // IllegalArgumentException 👍
        // Anyway, it's already too late
    }
}
