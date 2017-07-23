package com.github.srad.textimager.model.query;

/**
 * All queries must implement the {@link #executeImplementation(String)}
 * method and return a result of type T.
 */
abstract public class AbstractQueryExecutor<T, U> {
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

        T result = this.<T>executeImplementation(query);
        U resultSet = this.<T, U>getResultSet(result);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        return new ExecutionPlan<T, U>(result, elapsedTime, query, resultSet);
    }
}
