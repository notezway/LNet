package test;

/**
 * Created by slimon on 20-11-17.
 */
public class ProcessorTester {

    public static void main(String[] args) throws InterruptedException {

        final Object lock = new Object();

        Runnable runnable = () -> {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                } finally {
                    System.out.println("After wait");
                }
            }
        };

        Thread thread = new Thread(runnable);

        thread.start();

        Thread.sleep(2000);

        synchronized (lock) {
            lock.notify();
        }
    }
}
