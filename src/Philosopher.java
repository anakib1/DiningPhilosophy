import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Philosopher {

    private final static int MIN_IDLE_TIME = 500;
    private final static int MAX_IDLE_TIME = 1000;
    private static final Random rand = new Random();

    private Philosopher left;
    private Philosopher right;
    private final Object lMonitor = new Object();
    private final Object rMonitor = new Object();
    private Fork leftFork = Fork.DIRTY;
    private Fork rightFork;
    int id;
    int eatCount = 0;
    Queue<Philosopher> leftForkRequesters = new ConcurrentLinkedQueue<>();
    Queue<Philosopher> rightForkRequesters = new ConcurrentLinkedQueue<>();

    public Philosopher(int id) {
        this.id = id;
    }

    public void setLeft(Philosopher left) {
        this.left = left;
    }

    public void setRight(Philosopher right) {
        this.right = right;
    }

    public void think() throws Exception {
        System.out.println("Philosopher " + id + " is thinking.");
        Thread.sleep(rand.nextInt(MIN_IDLE_TIME, MAX_IDLE_TIME));
    }

    public void eat() throws Exception {
        System.out.println("Philosopher " + id + " is eating.");
        eatCount++;
        Thread.sleep(rand.nextInt(3_000, 6_000));
        leftFork = Fork.DIRTY;
        rightFork = Fork.DIRTY;
    }

    public void requestRightFork(Philosopher requester) {
        rightForkRequesters.add(requester);
    }

    public void requestLeftFork(Philosopher requester) {
        leftForkRequesters.add(requester);
    }

    private void acquireLeft() {
        synchronized (lMonitor) {
            leftFork = Fork.CLEAN;
        }
    }

    private void acquireRight() {
        synchronized (rMonitor) {
            rightFork = Fork.CLEAN;
        }
    }


    public void start() {
        System.out.println("Philosopher #" + id + " started.");
        try {
            while (true) {
                think();
                synchronized (rMonitor) {
                    if (rightFork == null)
                        right.requestLeftFork(this);
                }
                synchronized (lMonitor) {
                    if (leftFork == null)
                        left.requestRightFork(this);
                }
                synchronized (rMonitor) {
                    synchronized (lMonitor) {
                        if (leftFork != null && rightFork != null) {
                            eat();
                        }
                    }
                }

                synchronized (lMonitor) {
                    if (leftFork == Fork.DIRTY && !leftForkRequesters.isEmpty()) {
                        Philosopher requester = leftForkRequesters.poll();
                        requester.acquireRight();
                        leftFork = null;
                    }
                }

                synchronized (rMonitor) {
                    if (rightFork == Fork.DIRTY && !rightForkRequesters.isEmpty()) {
                        Philosopher requester = rightForkRequesters.poll();
                        requester.acquireLeft();
                        rightFork = null;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("ERROR - philosopher #" + id + " was interrupted.");
        }
    }
}
