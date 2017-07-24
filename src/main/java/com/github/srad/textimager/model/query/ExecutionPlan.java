package com.github.srad.textimager.model.query;

import java.util.HashMap;

/**
 * Contains the execution time, query and the resultSet of a query and maybe some additional meta
 * data about a query as the application evolves.
 * <p>
 * Result that is returned from {@link AbstractQueryExecutor#execute(String)} method.
 *
 * @param <T>
 */
final public class ExecutionPlan<T, U> {
    final public T result;
    final public U resultSet;
    final public String query;
    final public long iterations;
    public long time;
    private boolean isCached;
    private long cacheFetchTime;

    public boolean isCached() {
        return isCached;
    }

    public ExecutionPlan<T, U> setCached(boolean cached) {
        isCached = cached;
        return this;
    }

    public ExecutionPlan(final T result, final long time, final String query, final U resultSet, final long iterations) {
        this.result = result;
        this.time = time;
        this.query = query;
        this.resultSet = resultSet;
        this.iterations = iterations;
    }

    public HashMap<String, Object> toMap() {
        return toMap(isCached());
    }

    /**
     * Only expose selected fields as data that shall be passed around.
     *
     * @param isCached
     * @return
     */
    public HashMap<String, Object> toMap(boolean isCached) {
        HashMap map = new HashMap<String, Object>();
        map.put("resultSet", resultSet);
        map.put("time", time);
        map.put("query", query);
        map.put("iterations", iterations);
        map.put("isCached", isCached);
        map.put("cacheFetchTime", getCacheFetchTime());
        return map;
    }

    public long getCacheFetchTime() {
        return cacheFetchTime;
    }

    public ExecutionPlan setCacheFetchTime(long cacheFetchTime) {
        this.cacheFetchTime = cacheFetchTime;
        return this;
    }
}
