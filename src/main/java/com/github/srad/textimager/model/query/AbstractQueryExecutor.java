package com.github.srad.textimager.model.query;

/**
 * All queries must implement the {@link #executeImplementation(String)}
 * method and return a result of type T.
 */
abstract public class AbstractQueryExecutor {
    /**
     * Needs to be implemented by all objects that provide queries to the database.
     * @param query
     * @param <T>
     * @return
     */
    abstract protected <T> T executeImplementation(final String query);

    /**
     * Executes the actual query to the database.
     * @param query
     * @param <T>
     * @return
     */
    public <T> ExecutionPlan<T> execute(final String query) {
        long startTime = System.currentTimeMillis();

        T result = executeImplementation(query);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        return new ExecutionPlan<T>(result, elapsedTime, query);
    }
}
