package com.github.srad.textimager.storage.redis;

import com.github.srad.textimager.CasImporterConfig;
import com.github.srad.textimager.model.type.Document;
import com.github.srad.textimager.model.type.Token;
import com.github.srad.textimager.reader.type.ElementType;
import com.github.srad.textimager.storage.AbstractStorage;
import com.lambdaworks.redis.*;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisHashAsyncCommands;
import com.lambdaworks.redis.api.async.RedisSetAsyncCommands;
import com.github.srad.textimager.storage.Key;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RedisStorage extends AbstractStorage {

    final private RedisClient client;
    final private StatefulRedisConnection connection;

    public RedisStorage() {
        this.client = RedisStorage.createClient();
        this.connection = this.client.connect();
    }

    public static RedisClient createClient() {
        return RedisClient.create(new RedisURI("localhost", CasImporterConfig.ArdbPort, 10, TimeUnit.MINUTES));
    }

    @Override
    public Object[] unionScoredTypes(final String[] ids, final String type, final Long limit) throws InterruptedException, ExecutionException {
        HashMap<String, Double> unionCount = new HashMap<>();

        for (String id : ids) {
            String key = Key.docTypeCount(id, type);

            RedisFuture<List<ScoredValue<String>>> future = connection.async().zrevrangebyscoreWithScores(key, Range.create(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), Limit.create(0, limit));
            future.await(60, TimeUnit.SECONDS);

            for (ScoredValue<String> score : future.get()) {
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

    @Override
    public Map<String, String> paginateTitles(final int offset, final Long limit) throws InterruptedException, ExecutionException {
        final ScanArgs scanArgs = ScanArgs.Builder.limit(limit);

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

    @Override
    public Set<String> unionSet(final String elemenType, String[] elements) throws InterruptedException, ExecutionException {
        RedisSetAsyncCommands<String, String> cmd = connection.async();
        for (int i = 0; i < elements.length; i += 1) {
            elements[i] = Key.createUnionElementType(elemenType, elements[i]);
            System.out.println(elements[i]);
        }

        final RedisFuture<Set<String>> future = cmd.sunion(elements);
        future.await(60, TimeUnit.SECONDS);

        return future.get();
    }

    @Override
    public Set<String> intersectSet(final String elemenType, String[] elements) throws InterruptedException, ExecutionException {
        final RedisSetAsyncCommands<String, String> cmd = connection.async();

        // Convert to lookup keys
        for (int i = 0; i < elements.length; i += 1) {
            elements[i] = Key.createUnionElementType(elemenType, elements[i]);
        }

        final RedisFuture<Set<String>> future = cmd.sinter(elements);
        future.await(60, TimeUnit.SECONDS);

        return future.get();
    }

    @Override
    public ArrayList<Document> getDocs(String id) throws ExecutionException, InterruptedException {
        return getDocs(new String[] {id});
    }

    @Override
    public ArrayList<Document> getDocs(String[] ids) throws ExecutionException, InterruptedException {
        final RedisHashAsyncCommands<String, String> cmdHash = connection.async();
        ArrayList<Document> union = new ArrayList<>();

        for (String id : ids) {
            final RedisFuture<Map<String, String>> future = cmdHash.hgetall(Key.docMetadata(id));
            future.await(60, TimeUnit.SECONDS);
            union.add(new Document(future.get()));
        }

        return union;
    }

    @Override
    public Long scard(String type, String text) throws ExecutionException, InterruptedException {
        return (Long) connection.async().scard(Key.create("set", "union", type, text)).get();
    }

    @Override
    public String getElement(String documentId, String elementId, String elementKey, Class<? extends ElementType> type) throws ExecutionException, InterruptedException {
        RedisHashAsyncCommands<String, String> cmd = connection.async();
        return cmd.hget(Key.elementType(documentId, elementId, type), elementKey).get();
    }

    @Override
    public Set<String> getElementIds(String documentId, Class<? extends ElementType> type) throws ExecutionException, InterruptedException {
        RedisSetAsyncCommands<String, String> cmd = connection.async();
        return cmd.smembers(Key.elementTypeIdSet(documentId, type)).get();
    }

    @Override
    public ArrayList<Token> getElementTypes(String documentId, Class<? extends ElementType> type) throws ExecutionException, InterruptedException {
        return null;
        /*
        final RedisHashAsyncCommands<String, String> cmdHash = connection.async();

         connection.setAutoFlushCommands(false);

        List<RedisFuture<?>> futures = new ArrayList<>();
        ids.forEach(id -> {
            futures.add(cmdHash.hgetall(Key.elementTypeIdSet(documentId, type)));
        });

        connection.flushCommands();

        boolean result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS,
                futures.toArray(new RedisFuture[futures.size()]));
*/
    }

    @Override
    public void close() {
        connection.flushCommands();
        connection.close();
        client.shutdown();
    }
}
