package com.github.srad.metaquery.dbms.executor;

import com.github.srad.metaquery.dbms.ExecutionPlan;
import com.github.srad.metaquery.dbms.storage.AbstractStorage;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * All queries must implement the {@link #executeImplementation(String)}
 * method and return a result of type ExecuteType.
 */
abstract public class AbstractQueryExecutor<ExecuteType, ResultSetType, StorageType extends AbstractStorage> {

    /** For query-plan fetch iterations tracked by {@link #fetch(Supplier)} */
    private AtomicInteger iterations = new AtomicInteger();

    public StorageType storage;

    public AbstractQueryExecutor(Class<StorageType> storage) throws IllegalAccessException, InstantiationException {
        this.storage = storage.newInstance();
    }

    /**
     * Implementation required by all objects that provide queries to the database.
     * @param query
     * @return
     */
    abstract protected ExecuteType executeImplementation(final String query);

    /** Fetch the actual data from the result object. If it's already the resultset then return that.
     * @param result
     * @return
     */
    abstract protected ResultSetType fetchResultSet(ExecuteType result);

    /**
     * Wraps the database query into an {@link ExecutionPlan} to add additional information.
     * @param query
     * @return
     */
    public ExecutionPlan<ExecuteType, ResultSetType> execute(final String query) {
        long startTime = System.currentTimeMillis();
        iterations.set(0);

        ExecuteType result = this.<ExecuteType>executeImplementation(query);
        ResultSetType resultSet = this.<ExecuteType, ResultSetType>fetchResultSet(result);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        return new ExecutionPlan<ExecuteType, ResultSetType>(result, elapsedTime, query, resultSet, iterations.longValue());
    }

    /**
     * All iterations must call this container-function to get tracked in {@link ExecutionPlan#iterations}.
     * @param fetcher
     * @param <ResultType>
     * @return
     */
    protected <ResultType> ResultType fetch(Supplier<ResultType> fetcher) {
        iterations.incrementAndGet();
        return fetcher.get();
    }
}
