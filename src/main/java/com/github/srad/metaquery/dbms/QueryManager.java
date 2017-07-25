package com.github.srad.metaquery.dbms;

import com.github.srad.metaquery.dbms.executor.AbstractQueryExecutor;
import com.github.srad.metaquery.dbms.storage.AbstractStorage;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Container for caching and other upcoming thing.
 * Manages the cached queries right now.
 */
public class QueryManager<ExecutorType extends AbstractQueryExecutor, StorageType extends AbstractStorage> {
    private final ExecutorType executor;
    private final ConcurrentHashMap<String, ExecutionPlan> cachedQueries = new ConcurrentHashMap<>();

    public QueryManager(Class<ExecutorType> executorType, Class<StorageType> storageType) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        // getConstructor(storageType).newInstance(storageType) raises an error
        // although the compiler correctly inserts the types.
        this.executor = (ExecutorType) executorType.getConstructors()[0].newInstance(storageType);
    }

    public ExecutionPlan execute(String query) {
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

    public AbstractStorage getStorage() {
        return executor.storage;
    }

    public void purge() {
        cachedQueries.clear();
    }

    public void stop() {
        executor.storage.close();
    }
}
