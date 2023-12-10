import java.util.concurrent.locks.ReentrantLock;

public class ControlSignalQueue {

    private final int QUEUE_SIZE;

    private String[] controlSignal;
    private int front;
    private int back;

    private ReentrantLock lock;

    public ControlSignalQueue(int queueSize) {

        QUEUE_SIZE = queueSize;
        controlSignal = new String[QUEUE_SIZE];

        front = back = 0;

        lock = new ReentrantLock(true);

    }

    public String getNext() { // don't need lock because only 1 thread access this func at a time
        if (front == back) return null;
        String data = controlSignal[front];
        front = (front + 1) % QUEUE_SIZE;
        return data;
    }

    public void push(String signal) {
        try {
            lock.lock();
            controlSignal[back] = signal;
            back = (back + 1) % QUEUE_SIZE;
        } finally {
            lock.unlock();
        }
    }

}
