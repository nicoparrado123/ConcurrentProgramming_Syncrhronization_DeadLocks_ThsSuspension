package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BlackListChecker extends Thread {

    private final String ipaddress;
    private final int from;
    private final int to;
    private final List<Integer> results;
    private final AtomicInteger occurrences;
    private final int alarmCount;

    public BlackListChecker(String ipaddress, int from, int to, List<Integer> results, AtomicInteger occurrences, int alarmCount) {
        this.ipaddress = ipaddress;
        this.from = from;
        this.to = to;
        this.results = results;
        this.occurrences = occurrences;
        this.alarmCount = alarmCount;
    }

    @Override
    public void run() {
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        for (int i = from; i < to && occurrences.get() < alarmCount; i++) {
            if (skds.isInBlackListServer(i, ipaddress)) {
                results.add(i);
                occurrences.incrementAndGet();
            }
        }
    }
}
