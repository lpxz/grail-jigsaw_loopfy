package org.w3c.www.protocol.http.cache;

public abstract class CacheSweeper extends Thread {

    protected static final int STATE_CLEAN_STORED = 1;

    protected static final int STATE_FORCE_CLEAN_STORED = 2;

    protected static final int STATE_CLEAN_GENERATIONS = 3;

    protected static final int STATE_FORCE_CLEAN_GENERATIONS = 4;

    /** 
     * Used to trigger a signal
     */
    public abstract void signal();

    /**
     * change the state of the Sweeper
     * @param an integer, setting the new cache state
     */
    protected abstract void setState(int state);

    /**
     * collect the still stored resources (disk)
     * @param generation, the CacheGeneration to clean
     */
    protected abstract void collectStored(CacheGeneration generation);

    /**
     * collect the still stored resources (disk)
     * in the whole cache
     */
    protected abstract void collectStored();

    /**
     * collect the existing resources
     * @param generation, the CacheGeneration to clean
     * @param bytes, a long. The number of bytes to collect
     * @param check, a boolean. If true, then only the stale resources
     * will be removed
     * @return a long, the number of collected bytes
     */
    protected abstract long collectCached(CacheGeneration generation, long bytes, boolean check);

    /**
     * collect the existing resources
     * @param bytes, a long. The number of bytes to collect
     * @param check, a boolean. If true, then only the stale resources
     * will be removed
     * @return a long, the number of collected bytes
     */
    protected abstract long collectCached(long bytes, boolean check);

    /**
     * initialize the sweeper
     */
    public abstract void initialize(CacheFilter filter);
}
