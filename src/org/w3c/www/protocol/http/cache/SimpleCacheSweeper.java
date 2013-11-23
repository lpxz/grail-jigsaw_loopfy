package org.w3c.www.protocol.http.cache;

import java.util.Enumeration;
import java.io.PrintStream;

public class SimpleCacheSweeper extends CacheSweeper {

    private static final boolean debug = false;

    private static final int WAIT_MIN = 5000;

    private static final int WAIT_MAX = 60000;

    protected int state = STATE_CLEAN_STORED;

    private CacheFilter filter = null;

    private boolean signal = false;

    private long wait_time = 60000;

    /** 
     * Used to trigger a signal
     */
    public synchronized void signal() {
        signal = true;
        notifyAll();
    }

    public synchronized void waitSignal() {
        long sync_time = 0;
        long gencomp_time = 0;
        signal = false;
        wait_time = WAIT_MIN;
        edu.hkust.clap.monitor.Monitor.loopBegin(1105);
while (!signal) { 
edu.hkust.clap.monitor.Monitor.loopInc(1105);
{
            try {
                wait(wait_time);
            } catch (InterruptedException ex) {
                continue;
            }
            if (signal) break;
            if (debug) {
                System.out.println("# Sweeper waited for " + wait_time);
            }
            try {
                collectStored();
            } catch (Exception ex) {
            }
            CacheStore store = filter.getStore();
            wait_time = (long) (WAIT_MAX * (1 - store.getMRUGenerationRatio())) / state;
            wait_time = Math.max(WAIT_MIN, Math.min(wait_time, WAIT_MAX));
            if (debug) {
                System.out.println("# Sweeper will wait for " + wait_time);
                System.out.println("# Sweeper sync time " + sync_time);
                switch(state) {
                    case STATE_CLEAN_STORED:
                        System.out.println("State: STATE_CLEAN_STORED");
                        break;
                    case STATE_FORCE_CLEAN_STORED:
                        System.out.println("State: STATE_FORCE_CLEAN_STORED");
                        break;
                    case STATE_CLEAN_GENERATIONS:
                        System.out.println("State: STATE_CLEAN_GENERATIONS");
                        break;
                    case STATE_FORCE_CLEAN_GENERATIONS:
                        System.out.println("State: STATE_FORCE_CLEAN_GENERATIONS");
                        break;
                }
                System.out.println(store.toString());
                store.checkState();
            }
            sync_time += wait_time;
            gencomp_time += wait_time;
            if (gencomp_time >= store.getCompactGenerationDelay()) {
                gencomp_time = 0;
                store.compactGenerations();
            }
            if (sync_time >= store.getSyncDelay()) {
                sync_time = 0;
                store.sync();
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1105);

        signal = false;
    }

    public void run() {
while (true) { 
{
            try {
                waitSignal();
                garbageCollect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }} 

    }

    /**
     * Run the garbage collector.
     */
    public void garbageCollect() {
        long to_save;
        switch(state) {
            case CacheSweeper.STATE_CLEAN_STORED:
                try {
                    collectStored();
                } catch (Exception ex) {
                }
                ;
                break;
            case CacheSweeper.STATE_FORCE_CLEAN_STORED:
                synchronized (filter.getStore()) {
                    collectStored();
                }
                break;
            case CacheSweeper.STATE_CLEAN_GENERATIONS:
                to_save = filter.getStore().getRequiredByteNumber();
                try {
                    to_save -= collectCached(to_save, true);
                    to_save -= collectCached(to_save, false);
                    collectStored();
                } catch (Exception ex) {
                }
                break;
            case CacheSweeper.STATE_FORCE_CLEAN_GENERATIONS:
                synchronized (filter.getStore()) {
                    to_save = filter.getStore().getRequiredByteNumber();
                    to_save -= collectCached(to_save, true);
                    to_save -= collectCached(to_save, false);
                    collectStored();
                }
                break;
        }
        filter.getStore().updateSweeperPriority();
    }

    /**
     * change the state of the Sweeper
     * @param an integer, setting the new cache state
     */
    protected synchronized void setState(int state) {
        this.state = state;
    }

    /**
     * collect the still stored resources
     * @param generation, the CacheGeneration to clean
     */
    protected void collectStored(CacheGeneration generation) {
        Enumeration e = generation.getDeletedResources();
        CachedResource cr;
        edu.hkust.clap.monitor.Monitor.loopBegin(1107);
while (e.hasMoreElements()) { 
edu.hkust.clap.monitor.Monitor.loopInc(1107);
{
            cr = (CachedResource) e.nextElement();
            generation.deleteStored(cr);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1107);

    }

    /**
     * collect the still stored resources
     * in the whole cache
     * It will NOT block the cache during the process
     */
    protected void collectStored() {
        if (debug) System.out.println("*** Sweeper: collect stored");
        CacheGeneration gen;
        gen = (CacheGeneration) filter.getStore().getLRUGeneration();
        edu.hkust.clap.monitor.Monitor.loopBegin(1108);
while (gen != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(1108);
{
            collectStored(gen);
            gen = filter.getStore().getPrevGeneration(gen);
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1108);

    }

    /**
     * collect the existing resources
     * @param generation, the CacheGeneration to clean
     * @param bytes, a long. The number of bytes to collect
     * @param check, a boolean. If true, then only the stale resources
     * will be removed
     */
    protected long collectCached(CacheGeneration generation, long bytes, boolean check) {
        if (bytes > 0) {
            return generation.collectSpace(bytes, check);
        }
        return 0;
    }

    /**
     * collect the existing resources
     * @param bytes, a long. The number of bytes to collect
     * @param check, a boolean. If true, then only the stale resources
     * will be removed
     * @return a long, the number of collected bytes
     */
    protected long collectCached(long bytes, boolean check) {
        CacheGeneration gen, next;
        long collected = 0;
        CacheStore store = filter.getStore();
        gen = (CacheGeneration) store.getLRUGeneration();
        edu.hkust.clap.monitor.Monitor.loopBegin(1109);
while (gen != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(1109);
{
            next = filter.getStore().getPrevGeneration(gen);
            collected += gen.collectSpace(bytes - collected, check);
            if (collected >= bytes) {
                break;
            }
            gen = next;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(1109);

        return collected;
    }

    /**
     * initialize the sweeper
     */
    public void initialize(CacheFilter filter) {
        this.filter = filter;
        this.setDaemon(true);
        this.setPriority(3);
        this.setName("CacheSweeper");
    }
}
