package main.java.org.srad.textimager.net;

import com.google.gson.Gson;
import main.java.org.srad.textimager.redis.Key;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.*;

import static spark.Spark.*;

public class Rest {
    private static Jedis jedis = new Jedis("localhost");

    final public static String TITLES = "/doc/title/:id";
    final public static String CONTENT = "/doc/content/:id";
    final public static String TOP_LEMMAS = "/doc/:id/:type/:limit";

    final private static Gson gson = new Gson();

    public void start() {
        port(8080);
        routes();
    }

    private void routes() {
        get(TITLES, (request, response) -> jedis.hgetAll(Key.DocTitle), gson::toJson);

        get(CONTENT, (request, response) -> jedis.hmget(Key.DocContent, request.params(":id")), gson::toJson);

        get(TOP_LEMMAS, (request, response) -> {
            // Convert from jedis Tuple to SimpleEntry, for compatibility with java 5 they
            // convert element name to bytes (numbers) instead of keeping it as a String
            HashMap<String, Double> pairs = new HashMap<>();
            String[] ids = request.params(":id").split(",");

            // Union set of lemma frequencies
            for (String id: ids) {
                System.out.println(id);
                Set<Tuple> result = jedis.zrevrangeWithScores(Key.create("doc", id, request.params(":type")), 0L, Long.valueOf(request.params(":limit")));
                result.forEach(tuple -> pairs.put(tuple.getElement(), pairs.getOrDefault(tuple.getElement(), 0d) + tuple.getScore()));
            }

            return pairs;
        }, gson::toJson);

    }

    public void stop() {
        stop();
    }
}
