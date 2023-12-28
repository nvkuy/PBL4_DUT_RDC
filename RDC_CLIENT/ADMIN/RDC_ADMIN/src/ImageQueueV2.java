import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class ImageQueueV2 {

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
    private long[] owner;

    private BlockingQueue<BufferedImage> frames;
    private ReentrantLock lock;
    private AES aes;

    public ImageQueueV2(AES aes, long maxDelay) {

        this.aes = aes;

        MAX_DELAY = maxDelay;
        TIME_SPACE = 2 * maxDelay + 1;

        data = new ImageData[(int) TIME_SPACE];
        owner = new long[(int) TIME_SPACE];
        Arrays.fill(owner, 0);

        frames = new LinkedBlockingQueue<>();

        lock = new ReentrantLock(true);

    }

    public void push(byte[] rawData) throws Exception {

        long timeID = Util.bytesToLong(Arrays.copyOfRange(rawData, 0, 8));
        long curTime = System.currentTimeMillis();

        if (curTime - timeID > MAX_DELAY) return;

        int hashID = (int) (timeID % TIME_SPACE);
        if (data[hashID] == null || owner[hashID] < timeID) {
            try {
                lock.lock();
                if (data[hashID] == null || owner[hashID] < timeID) { // repeat check because another thread may already create it when get lock
                    data[hashID] = new ImageData();
                    owner[hashID] = timeID;
                }
            } finally {
                lock.unlock();
            }
        }

        if (data[hashID] != null && owner[hashID] == timeID) { // repeat check because another thread may already delete that node
            data[hashID].addPart(rawData);
            if (data[hashID].isCompleted()) {
                try {
                    lock.lock();
                    if (data[hashID] != null && data[hashID].isCompleted()) {
                        frames.put(data[hashID].getImage(aes));
                        data[hashID] = null;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

    }

    public BufferedImage getNextImage() throws Exception {
        return frames.take();
    }

}
