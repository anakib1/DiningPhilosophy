import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int num = 5;
        List<Philosopher> philosophers = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            philosophers.add(new Philosopher(i));
        }

        for (int i = 0; i < num; i++) {
            Philosopher left = philosophers.get((i + num - 1) % num);
            Philosopher right = philosophers.get((i + 1) % num);
            philosophers.get(i).setLeft(left);
            philosophers.get(i).setRight(right);
        }

        List<Thread> availableThreads = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> philosophers.get(finalI).start());
            availableThreads.add(thread);
            thread.start();
        }

        try {
            Thread.sleep(30_000);
        } catch (Exception ex) {
            System.out.println("ERROR - main thread was interrupted.");
        }

        for (Thread thread: availableThreads)
            thread.interrupt();

        for (Philosopher philosopher: philosophers){
            System.out.println("Philosopher #" + philosopher.id + " eaten " + philosopher.eatCount + " times.");
        }
    }
}