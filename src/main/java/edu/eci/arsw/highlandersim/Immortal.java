package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;
    private int health;
    private int defaultDamageValue;
    private final List<Immortal> immortalsPopulation;
    private final String name;
    private final Random r = new Random(System.currentTimeMillis());

    static volatile boolean paused = false;
    static volatile boolean stopped = false;
    static AtomicInteger pausedCount = new AtomicInteger(0);
    static AtomicInteger totalThreads = new AtomicInteger(0);
    static final Object pauseLock = new Object();

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
        totalThreads.incrementAndGet();
    }

    public void run() {
        while (!stopped) {
            synchronized (pauseLock) {
                if (paused) {
                    pausedCount.incrementAndGet();
                    pauseLock.notifyAll();
                    while (paused) {
                        try { pauseLock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    }
                    pausedCount.decrementAndGet();
                }
            }

            if (immortalsPopulation.size() < 2) continue;

            int myIndex = immortalsPopulation.indexOf(this);
            if (myIndex == -1) break;

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = (nextFighterIndex + 1) % immortalsPopulation.size();
            }

            Immortal im = immortalsPopulation.get(nextFighterIndex);
            this.fight(im);
        }
        totalThreads.decrementAndGet();
    }

    public void fight(Immortal i2) {
        Immortal first = this.name.compareTo(i2.name) < 0 ? this : i2;
        Immortal second = first == this ? i2 : this;

        synchronized (first) {
            synchronized (second) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                    if (i2.getHealth() <= 0) {
                        immortalsPopulation.remove(i2);
                    }
                } else {
                    updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
                }
            }
        }
    }

    public void changeHealth(int v) { health = v; }
    public int getHealth() { return health; }

    @Override
    public String toString() { return name + "[" + health + "]"; }
}
