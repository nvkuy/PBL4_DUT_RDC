import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class ImageQueue {

    private static final int MAX_SIZE = 1 << 16;

    private static final int FRONT = 1;

    private int[] timeIDHeap, numOfPart, imgByteLen, partReceived;
    private byte[][] IVpart;
    private byte[][][] imagePart;
    private int size;
    private ReentrantLock lock;

    /*

    image packet structure:

    - first 2 bytes: timeID (current time millisecond % TIME_RANGE)
    - next 2 bytes: partID (0 if it is header)
    - if packet is header:
        + next 2 bytes: number of parts which image was divided
        + other bytes: IV
    - else: image part data

     */

    public ImageQueue() {

        timeIDHeap = new int[MAX_SIZE + 1];
        numOfPart = new int[MAX_SIZE + 1];
        imgByteLen = new int[MAX_SIZE + 1];
        partReceived = new int[MAX_SIZE + 1];

        imagePart = new byte[MAX_SIZE + 1][16][];
        IVpart = new byte[MAX_SIZE + 1][];

        timeIDHeap[0] = Integer.MIN_VALUE;
        size = 0;

        lock = new ReentrantLock(true);

    }

    private int parent(int pos) {
        return pos / 2;
    }

    private int leftChild(int pos) {
        return (2 * pos);
    }

    private int rightChild(int pos) {
        return (2 * pos) + 1;
    }

    private boolean isLeaf(int pos) {
        return pos > (size / 2);
    }

    private void swap(int pos1, int pos2) {
        int tmp = timeIDHeap[pos1];
        timeIDHeap[pos1] = timeIDHeap[pos2];
        timeIDHeap[pos2] = tmp;
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

    public void push(byte[] rawData) {

        if (size >= MAX_SIZE)
            return;

        int timeID = bytesToInt(Arrays.copyOfRange(rawData, 0, 2));
        if (numOfPart[timeID] == 0 && partReceived[timeID] == 0) { // not in heap
            try {
                lock.lock();
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

        int partID = bytesToInt(Arrays.copyOfRange(rawData, 2, 4));
        if (partID == 0) { // header

            numOfPart[timeID] = bytesToInt(Arrays.copyOfRange(rawData, 4, 6));
            IVpart[timeID] = Arrays.copyOfRange(rawData, 6, rawData.length);

        } else { // normal image part

            if (imagePart[timeID][partID] != null) return; // udp package can be duplicate
            byte[] part = Arrays.copyOfRange(rawData, 4, rawData.length);
            imagePart[timeID][partID] = part;
            try {
                lock.lock();
                imgByteLen[timeID] += part.length;
                partReceived[timeID]++;
            } finally {
                lock.unlock();
            }

        }

    }

    public void pop() {

        int timeID;

        try {
            lock.lock();
            timeID = timeIDHeap[FRONT];
            timeIDHeap[FRONT] = timeIDHeap[size--];
            minHeapify(FRONT);
        } finally {
            lock.unlock();
        }

        numOfPart[timeID] = imgByteLen[timeID] = partReceived[timeID] = 0;
        for (int i = 0; i < 16; i++)
            imagePart[timeID][i] = null;
        IVpart[timeID] = null;

    }

    public boolean isCompleted(int timeID) {
//        System.out.println(partReceived[timeID] + "/" + numOfPart[timeID]);
        return (numOfPart[timeID] > 0) && (numOfPart[timeID] == partReceived[timeID]);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int getSize() {
        return size;
    }

    public BufferedImage getImage(int timeID, AES aes) throws Exception {

        if (!isCompleted(timeID))
            return null;

        byte[] data = new byte[imgByteLen[timeID]];
        int i = 0;
        for (int k = 1; k <= numOfPart[timeID]; k++) {
            for (int j = 0; j < imagePart[timeID][k].length; j++) {
                data[i] = imagePart[timeID][k][j];
                i++;
            }
        }

        InputStream is = new ByteArrayInputStream(aes.decrypt(data, IVpart[timeID]));
        return ImageIO.read(is);

    }

    public int getLatestTimeID() {

        try {
//            lock.lock();
            return timeIDHeap[FRONT];
        } finally {
//            lock.unlock();
        }

    }

    private static int bytesToInt(final byte[] b) {
        int result = 0;
        for (int i = 0; i <= 1; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

}
