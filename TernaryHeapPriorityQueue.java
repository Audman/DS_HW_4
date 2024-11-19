import java.util.ArrayList;
import java.util.Comparator;

public class TernaryHeapPriorityQueue<K, V> extends AbstractPriorityQueue<K, V>
{
    protected ArrayList<Entry<K, V>> heap = new ArrayList<>();

    public TernaryHeapPriorityQueue() { super(); }

    public TernaryHeapPriorityQueue(Comparator<K> comp) { super(comp); }

    public TernaryHeapPriorityQueue(K[] keys, V[] values) {
        super();
        for (int j = 0; j < Math.min(keys.length, values.length); j++)
            heap.add(new AbstractPriorityQueue.PQEntry<>(keys[j], values[j]));
        heapify();
    }

    protected int parent(int j) { return (j - 1) / 3; }

    protected int left(int j) { return 3 * j + 1; }
    protected int mid(int j) { return 3 * j + 2; }
    protected int right(int j) { return 3 * j + 3; }

    protected boolean hasLeft(int j) { return left(j) < heap.size(); }
    protected boolean hasMid (int j) { return mid(j) < heap.size(); }
    protected boolean hasRight(int j) { return right(j) < heap.size(); }

    protected void swap(int i, int j)
    {
        Entry<K, V> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    protected void upHeap(int j) {
        while (j > 0) {
            int p = parent(j);
            if (compare(heap.get(j), heap.get(p)) >= 0) break;
            swap(j, p);
            j = p;
        }
    }

    protected void downHeap(int j)
    {
        while (hasLeft(j))
        {
            int leftIndex = left(j);
            int smallChildIndex = leftIndex;
            if (hasMid(j)) {
                int midIndex = mid(j);
                if (compare(heap.get(leftIndex), heap.get(midIndex)) > 0)
                    smallChildIndex = midIndex;
            }
            if (hasRight(j)) {
                int rightIndex = right(j);
                if (compare(heap.get(leftIndex), heap.get(rightIndex)) > 0)
                    smallChildIndex = rightIndex;
            }
            if (compare(heap.get(smallChildIndex), heap.get(j)) >= 0) break;
            swap(j, smallChildIndex);
            j = smallChildIndex;
        }
    }

    protected void downheap(int j)
    {
        if (!hasLeft(j))
            return;

        int leftIndex = left(j);
        int small = leftIndex;

        if (hasMid(j)) {
            int midIndex = mid(j);
            if (compare(heap.get(leftIndex), heap.get(midIndex)) > 0)
                small = midIndex;
        }
        if (hasRight(j)) {
            int rightIndex = right(j);
            if (compare(heap.get(leftIndex), heap.get(rightIndex)) > 0)
                small = rightIndex;
        }

        if (compare(heap.get(small), heap.get(j)) >= 0) return;
        swap(j, small);
        j = small;

        downheap(j);
    }

    protected void heapify() {
        int startIndex = parent(size() - 1);
        for (int j = startIndex; j >= 0; j--)
            downHeap(j);
    }

    public int size() { return heap.size(); }

    public Entry<K, V> min() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    public Entry<K, V> insert(K key, V value) throws IllegalArgumentException {
        checkKey(key);
        Entry<K, V> newest = new AbstractPriorityQueue.PQEntry<>(key, value);

        heap.add(newest);
        upHeap(heap.size() - 1);
        return newest;
    }

    public Entry<K, V> removeMin()
    {
        if (heap.isEmpty()) return null;

        Entry<K, V> answer = heap.get(0);
        swap(0, heap.size() - 1);
        heap.remove(heap.size() - 1);
        downHeap(0);
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

    public static void differences(long bin, long tern)
    {
        long diff = (bin - tern)/1_000_000;

        System.out.println("Binary building time: " + bin);
        System.out.println("Ternary building time: " + tern);
        System.out.println("Difference: " + diff +"ms");
    }

    public static void main(String[] args)
    {
        Integer[] keys = new Integer[1_000_000];

        for(int i = 0; i < 1_000_000; i++)
        {
            keys[i] = i % 1000;
        }

        long startTime, finishTime;

        //-----------------------FILL----------------------

        startTime = System.nanoTime();

        HeapPriorityQueue<Integer, Integer> hpq =
            new HeapPriorityQueue<>(keys, keys);

        finishTime = System.nanoTime();

        long binaryTime = finishTime - startTime;

        startTime = System.nanoTime();

        TernaryHeapPriorityQueue<Integer, Integer> thpq =
            new TernaryHeapPriorityQueue<>(keys, keys);

        finishTime = System.nanoTime();

        differences(binaryTime, finishTime - startTime);

        //-----------------------KILL----------------------

        startTime = System.nanoTime();
        while(!hpq.isEmpty()) hpq.removeMin();
        finishTime = System.nanoTime();

        binaryTime = finishTime - startTime;

        startTime = System.nanoTime();
        while(!thpq.isEmpty()) thpq.removeMin();
        finishTime = System.nanoTime();

        differences(binaryTime, finishTime - startTime);

        // Ternary heap is a bit faster to build and approximately twice faster to empty
    }
}
