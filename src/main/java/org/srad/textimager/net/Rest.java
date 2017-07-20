package main.java.org.srad.textimager.net;

import com.google.gson.Gson;
import com.lambdaworks.redis.*;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisHashAsyncCommands;
import main.java.org.srad.textimager.CasImporterConfig;
import main.java.org.srad.textimager.storage.Key;
import main.java.org.srad.textimager.storage.redis.RedisStorage;

import static spark.Spark.*;

public class Rest {
    final public static String RouteDoc = "/doc/:id";
    final public static String RoutePaginateDocIdTypes = "/doc/:id/:type/:limit";
    final public static String RoutePaginateDocIdAndTitle = "/doc/title/:limit/:offset";
    final public static String RouteSetOperation = "/doc/set/:operator/:type/:element";

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
        routes();
    }

    private void routes() {
        final RedisHashAsyncCommands<String, String> hash = connection.async();

        get(RouteDoc, (request, response) -> hash.hgetall(Key.docMetadata(request.params(":id"))).get(), gson::toJson);

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
            final String[] elements = request.params(":element").split(",");
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
}