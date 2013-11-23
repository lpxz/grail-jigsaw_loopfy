package org.w3c.jigsaw.http.socket;

public class SocketClientFactoryStats {

    SocketClientFactory pool = null;

    private static final String __avg[] = { "Light", "Average", "High", "Dead" };

    public int getFreeConnectionsCount() {
        return pool.freeCount;
    }

    public int getIdleConnectionsCount() {
        return pool.idleCount;
    }

    public int getClientCount() {
        return pool.clientCount;
    }

    public int getLoadAverage() {
        return pool.loadavg;
    }

    public String getServerLoad() {
        return __avg[pool.loadavg - 1];
    }

    public SocketClientFactoryStats(SocketClientFactory pool) {
        this.pool = pool;
    }
}
