package main.java.org.srad.textimager.storage.redis;

import com.lambdaworks.redis.*;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisSetAsyncCommands;
import main.java.org.srad.textimager.CasImporterConfig;
import main.java.org.srad.textimager.storage.Key;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RedisStorage {

    final StatefulRedisConnection connection;

    public RedisStorage(final StatefulRedisConnection connection) {
        this.connection = connection;
    }

    public static RedisClient createClient() {
        return RedisClient.create(new RedisURI("localhost", CasImporterConfig.ArdbPort, 10, TimeUnit.MINUTES));
    }

    public Object[] unionScoredTypes(final String[] ids, final String type, final Long limit) throws InterruptedException, ExecutionException {
        HashMap<String, Double> unionCount = new HashMap<>();

        for(String id: ids) {
            String key = Key.docTypeCount(id, type);

            RedisFuture<List<ScoredValue<String>>> future = connection.async().zrevrangebyscoreWithScores(key, Range.create(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), Limit.create(0, limit));
            future.await(60, TimeUnit.SECONDS);

            for(ScoredValue<String>score: future.get()) {
                unionCount.compute(score.getValue(), (text, storedScore) -> storedScore == null ? score.getScore() : storedScore + score.getScore());
            }
        }

        return unionCount
                .entrySet()
                .stream()
                .limit(limit)
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .toArray();
    }

    public Map<String, String> paginateTitles(final int offset, final Long limit) throws InterruptedException, ExecutionException {
       ScanArgs scanArgs = ScanArgs.Builder.limit(limit);

        RedisFuture<MapScanCursor<String, String>> future = connection.async().hscan(Key.DocTitle, scanArgs);
        future.await(60, TimeUnit.SECONDS);
        MapScanCursor<String, String> cursor = future.get();

        for (int i = 0; i < offset; i += 1) {
            future = connection.async().hscan(Key.DocTitle, cursor, scanArgs);
            future.await(60, TimeUnit.SECONDS);
            cursor = future.get();
        }

        return cursor.getMap();
    }

    public Set<String> unionSet(final String elemenType, String[] elements) throws InterruptedException, ExecutionException {
        RedisSetAsyncCommands<String, String> cmd = connection.async();
        for(int i=0; i < elements.length; i += 1) {
            elements[i] = Key.createUnionElementType(elemenType, elements[i]);
            System.out.println(elements[i]);
        }
        RedisFuture<Set<String>> future = cmd.sunion(elements);
        future.await(60, TimeUnit.SECONDS);

        return future.get();
    }

    public Set<String> intersectSet(final String elemenType, String[] elements) throws InterruptedException, ExecutionException {
        RedisSetAsyncCommands<String, String> cmd = connection.async();
        for(int i=0; i < elements.length; i += 1) {
            elements[i] = Key.createUnionElementType(elemenType, elements[i]);
            System.out.println(elements[i]);
        }
        RedisFuture<Set<String>> future = cmd.sinter(elements);
        future.await(60, TimeUnit.SECONDS);

        return future.get();
    }
}
