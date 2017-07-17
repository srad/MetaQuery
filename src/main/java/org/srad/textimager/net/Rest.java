package main.java.org.srad.textimager.net;

import com.google.gson.Gson;

import static spark.Spark.*;

public class Rest {
    final public static String RouteDoc = "/doc/:id";
    final public static String RouteDocGetByTimeWithLimit = "/doc/:id/:type/:limit";

    final private static Gson gson = new Gson();

    public void start() {
        port(8080);
        routes();
    }

    private void routes() {
        /*
        get(RouteDoc, (request, response) -> storage.getHashSet(Key.create("doc", request.params(":id"), "meta")), gson::toJson);

        get(RouteDocGetByTimeWithLimit, (request, response) -> {
            // Convert from jedis Tuple to SimpleEntry, for compatibility with java 5 they
            // convert element name to bytes (numbers) instead of keeping it as a String
            HashMap<String, Double> pairs = new HashMap<>();
            String[] queryKeys = Arrays.stream(request.params(":id").split(","))
                    .map(id -> Key.create("doc", id, request.params(":type")))
                    .toArray(String[]::new);

            System.out.printf("GET: /doc/%s/%s/%s\n", request.params(":id"), request.params(":type"), request.params(":limit"));
            System.out.println(String.join(", ", queryKeys));

            return storage.getTops(queryKeys);
        }, gson::toJson);
        */

    }

    public void stop() {
        stop();
    }
}
