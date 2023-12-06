import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class ImageQueue {

    /*

    image packet structure:

    - first 8 bytes: timeID
    - next 2 bytes: partID (0 if it is header)
    - if packet is header:
        + next 2 bytes: number of parts which image was divided
        + other bytes: IV
    - else: image part data

     */

    private final long MAX_DELAY;
    private final long TIME_SPACE;

    private ImageData[] data;
    private TimeIDHeap timeIDHeap;

    private ReentrantLock lock;

    public ImageQueue(long maxDelay) {

        MAX_DELAY = maxDelay;
        TIME_SPACE = 2 * maxDelay + 1;

        data = new ImageData[(int)TIME_SPACE];
        timeIDHeap = new TimeIDHeap((int)TIME_SPACE);

        lock = new ReentrantLock(true);

    }

    public void push(byte[] rawData) {

        long timeID = Util.bytesToLong(Arrays.copyOfRange(rawData, 0, 8));
        long curTime = System.currentTimeMillis();

        if (curTime - timeID > MAX_DELAY) return;

        int hashID = (int)(timeID % TIME_SPACE);
        try {
            lock.lock();
            if (data[hashID] == null) {
                data[hashID] = new ImageData();
                timeIDHeap.push(timeID);
            }
        } finally {
            lock.unlock();
        }

        if (data[hashID] != null) // repeat check because another thread may already delete that node
            data[hashID].addPart(rawData);

    }

    public BufferedImage getNextImage(AES aes) throws Exception {

        long curTime = System.currentTimeMillis();
        while (true) {

            if (timeIDHeap.isEmpty()) break;
            long id = timeIDHeap.getLatestTimeID();
            int hashID = (int)(id % TIME_SPACE);
            if (curTime - id <= MAX_DELAY) break;

            try {
                lock.lock();
                if (data[hashID] != null) { // repeat check because when allow to lock the condition may not correct anymore
                    timeIDHeap.pop();
                    data[hashID] = null;
                }
            } finally {
                lock.unlock();
            }

        }

        if (timeIDHeap.isEmpty()) return null;

        long id = timeIDHeap.getLatestTimeID();
        int hashID = (int) (id % TIME_SPACE);

        if (data[hashID] == null || !data[hashID].isCompleted()) return null;

        try {
            lock.lock();
            if (data[hashID] == null || !data[hashID].isCompleted()) return null; // repeat check because when allow to lock the condition may not correct anymore
            BufferedImage img = data[hashID].getImage(aes);
            timeIDHeap.pop();
            data[hashID] = null;
            return img;
        } finally {
            lock.unlock();
        }

    }

}
