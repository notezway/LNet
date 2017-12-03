package test;

/**
 * Created by slimon on 20-11-17.
 */
public class ProcessorTester {

    public static void main(String[] args) throws InterruptedException {

        final String[] s = {""};

        final Object lock = new Object();

        Runnable runnable = () -> {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                } finally {
                    s[0] += "1";
                }
            }
        };

        Thread thread = new Thread(runnable);

        thread.start();

        Thread.sleep(1000);

        synchronized (lock) {
            lock.notify();
            s[0] += "2";
        }
        s[0] += "3";
        Thread.yield();
        synchronized (lock) {
            s[0] += "4";
        }
        Thread.sleep(1);
        synchronized (lock) {
            s[0] += "5";
        }
        Thread.sleep(500);
        System.out.println(s[0]);
    }
}
