import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class TimeIDHeap {

    private final int MAX_SIZE;
    private static final int FRONT = 1;
    private long[] timeIDHeap;
    private int size;

    private ReentrantLock lock;

    public TimeIDHeap(int maxSize) {

        MAX_SIZE = maxSize;

        timeIDHeap = new long[MAX_SIZE + 1];
        timeIDHeap[0] = Integer.MIN_VALUE;
        size = 0;

        lock = new ReentrantLock(true);

    }

    private static int parent(int pos) {
        return pos / 2;
    }

    private static int leftChild(int pos) {
        return (2 * pos);
    }

    private static int rightChild(int pos) {
        return (2 * pos) + 1;
    }

    private boolean isLeaf(int pos) {
        return pos > (size / 2);
    }

    private void swap(int pos1, int pos2) {
        try {
            lock.lock();
            long tmp = timeIDHeap[pos1];
            timeIDHeap[pos1] = timeIDHeap[pos2];
            timeIDHeap[pos2] = tmp;
        } finally {
            lock.unlock();
        }
    }

    private void minHeapify(int pos) {

        if (!isLeaf(pos)) {

            int swapPos = pos;
            if (rightChild(pos) <= size)
                swapPos = timeIDHeap[leftChild(pos)] < timeIDHeap[rightChild(pos)] ? leftChild(pos) : rightChild(pos);
            else
                swapPos = leftChild(pos);

            if (timeIDHeap[pos] > timeIDHeap[leftChild(pos)] || timeIDHeap[pos] > timeIDHeap[rightChild(pos)]) {
                swap(pos, swapPos);
                minHeapify(swapPos);
            }

        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int getSize() {
        return size;
    }

    public long getLatestTimeID() {
        try {
            lock.lock();
            return timeIDHeap[FRONT];
        } finally {
            lock.unlock();
        }
    }

    public void push(long timeID) {

        try {

            lock.lock();

            if (size >= MAX_SIZE)
                return;

            timeIDHeap[++size] = timeID;
            int current = size;

            while (timeIDHeap[current] < timeIDHeap[parent(current)]) {
                swap(current, parent(current));
                current = parent(current);
            }

        } finally {
            lock.unlock();
        }

    }

    public void pop() {

        try {
            lock.lock();
            timeIDHeap[FRONT] = timeIDHeap[size--];
            minHeapify(FRONT);
        } finally {
            lock.unlock();
        }

    }

}
