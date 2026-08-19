package edu.eci.arst.concprg.prodcons;

import java.util.Queue;
import java.util.Random;
public class Producer extends Thread {

    private Queue<Integer> queue = null;

    private int dataSeed = 0;
    private Random rand=null;
    private final long stockLimit;

    public Producer(Queue<Integer> queue,long stockLimit) {
        this.queue = queue;
        rand = new Random(System.currentTimeMillis());
        this.stockLimit=stockLimit;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (queue) {
                while (queue.size() >= stockLimit) {
                    try { queue.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
                dataSeed = dataSeed + rand.nextInt(100);
                System.out.println("Producer added " + dataSeed);
                queue.add(dataSeed);
                queue.notifyAll();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
