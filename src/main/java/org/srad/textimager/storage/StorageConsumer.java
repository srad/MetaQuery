package main.java.org.srad.textimager.storage;

import com.lambdaworks.redis.LettuceFutures;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisHashAsyncCommands;
import com.lambdaworks.redis.api.async.RedisSortedSetAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisServerCommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class StorageConsumer implements Runnable {
    final private ArrayBlockingQueue<AbstractElementStore> queue;
    final private RedisClient client = RedisClient.create("redis://localhost");
    final private StatefulRedisConnection<String, String> connection = client.connect();

    public StorageConsumer(ArrayBlockingQueue<AbstractElementStore> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            RedisServerCommands s = connection.sync();
            s.flushall();

            connection.setAutoFlushCommands(false);
            RedisHashAsyncCommands<String, String> cmdHash = connection.async();
            RedisSortedSetAsyncCommands<String, String> cmdSorted = connection.async();

            // Pipe
            List<RedisFuture<?>> futures = new ArrayList<>();

            while (true) {
                try {
                    AbstractElementStore element = queue.take();

                    if (element.isPoisonPill()) {
                        break;
                    }

                    if (futures.size() > 600) {
                        // send commands
                        connection.flushCommands();

                        // synchronization example: Wait until all futures complete
                        boolean result = LettuceFutures.awaitAll(10, TimeUnit.MINUTES, futures.toArray(new RedisFuture[futures.size()]));
                        futures.clear();
                    }

                    // TODO: Can be handled more natively ...
                    switch (element.getClass().getSimpleName()) {
                        case "MapStorage":
                            futures.add(cmdHash.hmset(element.key, (HashMap<String, String>) element.data));
                            break;
                        case "SortedSetStorage":
                            final Map<String, Long> set = (Map<String, Long>) element.data;
                            // combine in one pipe
                            for(Map.Entry<String, Long> e: set.entrySet()) {
                                futures.add(cmdSorted.zadd(element.key, e.getValue(), e.getKey()));
                            }
                            break;
                        default:
                            System.err.printf("ERROR: Unknown storage object: %s\n", element.getClass().getSimpleName());
                    }

                } catch (Exception e) {
                    System.err.printf("ERROR: StorageConsumer: %s\n", e.getMessage());
                }
            }
        } finally {
            connection.close();
        }
    }
}
