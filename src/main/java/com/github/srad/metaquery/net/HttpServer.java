package com.github.srad.metaquery.net;

import com.github.srad.metaquery.MetaQueryConfig;
import com.github.srad.metaquery.dbms.QueryManager;
import com.github.srad.metaquery.dbms.executor.AbstractQueryExecutor;
import com.github.srad.metaquery.dbms.storage.AbstractStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.SparkBase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

/**
 * Notice that some methods are using {@link AbstractStorage} calls and
 * and just one routes receives from the client any query that is supported
 * by the {@link com.github.srad.metaquery.dbms.executor.AbstractQueryExecutor} implementation.
 * <p>
 * If the {@link com.github.srad.metaquery.dbms.executor.AbstractQueryExecutor} implementation does support queries to provide the resultset
 * for all the other routes then all routes could also execute generic queries.
 * <p>
 * Until then a direct call to the {@link AbstractStorage} interface is a direct and low level call
 * with predictable performance. Otherwise we have to rely on optimizations within the {@link AbstractQueryExecutor}
 * implementation.
 *
 * @param <ExecutorType>
 * @param <StorageType>
 */
public class HttpServer<ExecutorType extends AbstractQueryExecutor, StorageType extends AbstractStorage> {

    final public static String RouteQueryDoc = "/doc/query/:query";
    final public static String RouteDoc = "/doc/:id";
    final public static String RoutePaginateDocIdTypes = "/doc/:id/:type/:limit";
    final public static String RoutePaginateDocIdAndTitle = "/doc/title/:limit/:offset";
    final public static String RouteSetOperation = "/set/:operator/:type/:text";
    final public static String RouteSetCard = "/set/card/:type/:text";

    final private static Gson gson = new GsonBuilder().serializeNulls().create();

    final private QueryManager<ExecutorType, StorageType> queryManager;

    public HttpServer(Class<ExecutorType> executorType, Class<StorageType> storageType) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        this.queryManager = new QueryManager<>(executorType, storageType);
    }

    public void start() {
        SparkBase.port(MetaQueryConfig.WebServerPort);
        config();
        routes();
    }

    private void config() {
        SparkBase.staticFileLocation("/public");
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
    }

    private void routes() {
        // list of accessible routes
        get("/explain", ((request, response) -> {
            response.type("application/json");
            return getRoutes();
        }), gson::toJson);

        get(RouteDoc, (request, response) -> queryManager.getStorage().getDocs(request.params(":id").split(",")), gson::toJson);

        get(RouteSetCard, (request, response) -> queryManager.getStorage().scard(request.params(":type"), request.params(":text")));

        get(RoutePaginateDocIdAndTitle, (request, response) -> {
            final int offset = Integer.valueOf(request.params(":offset"));
            final Long limit = Long.valueOf(request.params(":limit"));

            return queryManager.getStorage().paginateTitles(offset, limit);
        }, gson::toJson);

        get(RoutePaginateDocIdTypes, ((request, response) -> {
            final String[] ids = request.params(":id").split(",");
            final String type = request.params(":type");
            final Long limit = Long.valueOf(request.params(":limit"));

            return queryManager.getStorage().unionScoredTypes(ids, type, limit);
        }), gson::toJson);

        get(RouteSetOperation, (request, response) -> {
            final String[] elements = request.params(":text").split(",");
            final String type = request.params(":type");

            switch (request.params(":operator")) {
                case "union":
                    return queryManager.getStorage().unionSet(type, elements);
                case "intersect":
                    return queryManager.getStorage().intersectSet(type, elements);
                default:
                    throw new Exception("Operation not allowed");
            }
        }, gson::toJson);

        get(RouteQueryDoc, (request, response) -> {
            final String query = java.net.URLDecoder.decode(request.params(":query"), "UTF-8");
            response.type("application/json");

            return queryManager.execute(query).toMap();
        }, gson::toJson);
    }

    public void stop() {
        queryManager.stop();
        SparkBase.stop();
    }

    /**
     * Main route use reflection to show all possible routes.
     *
     * @return
     */
    private static List<String> getRoutes() {
        Field[] declaredFields = HttpServer.class.getDeclaredFields();
        List<String> routeFields = new ArrayList<>();

        for (Field field : declaredFields) {
            try {
                if (field.getName().startsWith("Route")) {
                    routeFields.add((String) field.get(field));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return routeFields;
    }
}