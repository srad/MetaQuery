package com.github.srad.textimager.net;

import com.github.srad.textimager.CasImporterConfig;
import com.github.srad.textimager.storage.redis.RedisStorage;
import com.google.gson.Gson;
import com.lambdaworks.redis.*;
import com.lambdaworks.redis.api.StatefulRedisConnection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class Rest {
    final public static String RouteDoc = "/doc/:id";
    final public static String RoutePaginateDocIdTypes = "/doc/:id/:type/:limit";
    final public static String RoutePaginateDocIdAndTitle = "/doc/title/:limit/:offset";
    final public static String RouteSetOperation = "/set/:operator/:type/:text";
    final public static String RouteSetCard = "/set/card/:type/:text";

    final private RedisClient client;
    final private StatefulRedisConnection<String, String> connection;

    final private static Gson gson = new Gson();

    final RedisStorage storage;

    public Rest() {
        this.client = RedisStorage.createClient();
        this.connection = this.client.connect();
        storage = new RedisStorage(this.connection);
    }

    public void start() {
        port(CasImporterConfig.WebServerPort);
        config();
        routes();
    }

    private void config() {
        options("/*",
                (request, response) -> {
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
        // Documentation
        get("/", ((request, response) -> getRoutes()), gson::toJson);

        get(RouteDoc, (request, response) -> storage.getDocs(request.params(":id").split(",")), gson::toJson);

        get(RouteSetCard, (request, response) -> storage.scard(request.params(":type"), request.params(":text")));

        get(RoutePaginateDocIdAndTitle, (request, response) -> {
            final int offset = Integer.valueOf(request.params(":offset"));
            final Long limit = Long.valueOf(request.params(":limit"));

            return storage.paginateTitles(offset, limit);
        }, gson::toJson);

        get(RoutePaginateDocIdTypes, ((request, response) -> {
            final String[] ids = request.params(":id").split(",");
            final String type = request.params(":type");
            final Long limit = Long.valueOf(request.params(":limit"));

            return storage.unionScoredTypes(ids, type, limit);
        }), gson::toJson);

        get(RouteSetOperation, (request, response) -> {
            final String[] elements = request.params(":text").split(",");
            final String type = request.params(":type");
            System.out.println(String.join(",", elements));

            switch (request.params(":operator")) {
                case "union":
                    return storage.unionSet(type, elements);
                case "intersect":
                    return storage.intersectSet(type, elements);
                default:
                    throw new Exception("Operation not allowed");
            }
        }, gson::toJson);
    }

    public void stop() {
        stop();
    }

    /**
     * Main route use reflection to show all possible routes.
     *
     * @return
     */
    private static List<String> getRoutes() {
        Field[] declaredFields = Rest.class.getDeclaredFields();
        List<String> routeFields = new ArrayList<>();

        for (Field field : declaredFields) {
            System.out.println(field.getName());
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