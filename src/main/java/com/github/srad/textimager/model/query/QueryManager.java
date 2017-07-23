package com.github.srad.textimager.model.query;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Container for caching and other upcoming thing.
 * Manages the cached queries right now.
 */
public class QueryManager {
    private ConcurrentHashMap<String, ExecutionPlan> cachedQueries = new ConcurrentHashMap<>();

    public ExecutionPlan execute(AbstractQueryExecutor executor, String query) {
        if (cachedQueries.containsKey(query)) {
            long startTime = System.currentTimeMillis();

            ExecutionPlan plan = cachedQueries.get(query);

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;

            plan.setCached(true);
            plan.setCacheFetchTime(elapsedTime);
            return plan;
        }
        ExecutionPlan plan = executor.execute(query);
        cachedQueries.put(query, plan);
        return plan;
    }

    public void purge() {
        cachedQueries.clear();
    }
}
