package edu.eci.arst.concprg.prodcons;

import java.util.Queue;

public class Consumer extends Thread{
    
    private Queue<Integer> queue;
    
    
    public Consumer(Queue<Integer> queue){
        this.queue=queue;        
    }
    
    @Override
    public void run() {
        while (true) {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try { queue.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
                System.out.println("Consumer consumes " + queue.poll());
                queue.notifyAll();
            }
        }
    }
}
