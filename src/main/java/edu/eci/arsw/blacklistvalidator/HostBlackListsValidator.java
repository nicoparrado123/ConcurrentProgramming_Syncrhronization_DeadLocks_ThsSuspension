package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private static final int NUM_THREADS = 4;
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

    public List<Integer> checkHost(String ipaddress) {
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int total = skds.getRegisteredServersCount();
        int chunk = total / NUM_THREADS;

        List<Integer> results = new CopyOnWriteArrayList<>();
        AtomicInteger occurrences = new AtomicInteger(0);
        BlackListChecker[] threads = new BlackListChecker[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            int from = i * chunk;
            int to = (i == NUM_THREADS - 1) ? total : from + chunk;
            threads[i] = new BlackListChecker(ipaddress, from, to, results, occurrences, BLACK_LIST_ALARM_COUNT);
            threads[i].start();
        }

        for (BlackListChecker t : threads) {
            try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        if (occurrences.get() >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Occurrences found: {0}", occurrences.get());
        return results;
    }
}
