package com.github.srad.textimager.model.query;

import java.util.HashMap;

/**
 * Contains the execution time, query and the resultset of a query and maybe some additional meta
 * data about a query as the application evolves.
 *
 * Result that is returned from {@link AbstractQueryExecutor#execute(String)} method.
 * @param <T>
 */
final public class ExecutionPlan<T> {
    final public T result;
    final public long time;
    final public String query;

    public ExecutionPlan(final T result, final long time, final String query) {
        this.result = result;
        this.time = time;
        this.query = query;
    }

    @Override
    public String toString() {
        return new HashMap<String, String>() {{
            put("time", String.valueOf(time));
            put("query", query);
            put("result", result.toString());
        }}.toString();
    }
}
