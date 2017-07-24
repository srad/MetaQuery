package com.github.srad.textimager.model.query;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * All queries must implement the {@link #executeImplementation(String)}
 * method and return a result of type T.
 */
abstract public class AbstractQueryExecutor<T, U> {

    /** For query-plan fetch iterations */
    private AtomicLong iterations = new AtomicLong();

    /**
     * Implementation required by all objects that provide queries to the database.
     * @param query
     * @return
     */
    abstract protected T executeImplementation(final String query);

    /** Fetch the actual data from the result object. If it's already the resultset then return that.
     * @param result
     * @return
     */
    abstract protected U getResultSet(T result);

    /**
     * Wraps the database query into an {@link ExecutionPlan} to add additional information.
     * @param query
     * @return
     */
    public ExecutionPlan<T, U> execute(final String query) {
        long startTime = System.currentTimeMillis();
        iterations.set(0);

        T result = this.<T>executeImplementation(query);
        U resultSet = this.<T, U>getResultSet(result);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        return new ExecutionPlan<T, U>(result, elapsedTime, query, resultSet, iterations.longValue());
    }

    /** All loop and callbacks can use this functional wrapper to get counted within the query-plan. */
    protected <A> A fetch(Supplier<A> fetcher) {
        iterations.incrementAndGet();
        return fetcher.get();
    }
}
