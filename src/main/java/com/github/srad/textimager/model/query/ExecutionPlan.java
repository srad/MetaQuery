package com.github.srad.textimager.model.query;

import java.util.HashMap;

/**
 * Contains the execution time, query and the resultSet of a query and maybe some additional meta
 * data about a query as the application evolves.
 *
 * Result that is returned from {@link AbstractQueryExecutor#execute(String)} method.
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

    public HashMap<String, Object> toMap(boolean isCached) {
        // The #result object is not serialized to string, since it's typically complex
        return new HashMap<String, Object>() {{
            put("time", time);
            put("query", query);
            put("result", resultSet.toString());
            put("cached", isCached);
            put("cacheFetchTime", getCacheFetchTime());
        }};
    }

    public long getCacheFetchTime() {
        return cacheFetchTime;
    }

    public ExecutionPlan setCacheFetchTime(long cacheFetchTime) {
        this.cacheFetchTime = cacheFetchTime;
        return this;
    }
}
