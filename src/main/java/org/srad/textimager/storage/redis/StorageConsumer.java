package main.java.org.srad.textimager.storage.redis;

import com.lambdaworks.redis.LettuceFutures;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.async.RedisHashAsyncCommands;
import com.lambdaworks.redis.api.async.RedisSortedSetAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisServerCommands;
import main.java.org.srad.textimager.storage.AbstractConsumer;
import main.java.org.srad.textimager.storage.Key;
import main.java.org.srad.textimager.storage.type.AbstractStorageCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Notice the redis server will reach up to 16GB of memory usage for an full import of ~2GB of data.
 * It's by far the fastest but holds the data entirely in memory, while still be durable on disk (append-only mode).
 *
 * Redis compatible storage-engines - like Ardb - will allow to use this same client but with lower memory usage
 * but with noticeable slower performance. A test has been run with the leveldb and rocksdb storage-backend (compiled from master)
 * Rocksdb seems to be faster.
 *
 *
 * @see <a href="https://github.com/yinqiwen/ardb">ardb</a>
 */
public class StorageConsumer extends AbstractConsumer {
    final private RedisClient client;
    final private StatefulRedisConnection<String, String> connection;

    final private List<RedisFuture<?>> futures = new ArrayList<>();

    final private static int PipeSize = 100;

    public StorageConsumer(ArrayBlockingQueue<AbstractStorageCommand> queue) {
        super(queue);
        this.client = RedisStorage.createClient();
        this.connection = client.connect();
    }

    @Override
    public void run() {
        try {
            RedisServerCommands s = connection.sync();
            s.flushall();

            connection.setAutoFlushCommands(false);
            RedisHashAsyncCommands<String, String> cmdHash = connection.async();
            RedisSortedSetAsyncCommands<String, String> cmdSorted = connection.async();
            RedisAsyncCommands<String, String> cmd = connection.async();

            // Pipe


            while (true) {
                try {
                    AbstractStorageCommand element = queue.take();

                    if (element.isPoisonPill()) {
                        break;
                    }

                    if (futures.size() > PipeSize) {
                      flushFutures();
                    }

                    // TODO: Can be handled more natively ...
                    switch (element.getClass().getSimpleName()) {
                        case "MapCommand":
                        futures.add(cmdHash.hmset(element.key, (HashMap<String, String>) element.data));
                        break;
                        case "SortedSetCommand":
                        final Map<String, Long> set = (Map<String, Long>) element.data;
                        // combine in one pipe
                        for(Map.Entry<String, Long> e: set.entrySet()) {
                            futures.add(cmdSorted.zadd(element.key, e.getValue(), e.getKey()));
                        }
                        break;
                        case "DocumentCommand":
                            futures.add(cmdHash.hmset(element.key, (HashMap<String, String>) element.data));
                            futures.add(cmd.incrby(Key.DocCount, 1));
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

    private void flushFutures() {
        // send commands
        connection.flushCommands();

        // synchronization example: Wait until all futures complete
        boolean result = LettuceFutures.awaitAll(10, TimeUnit.MINUTES, futures.toArray(new RedisFuture[futures.size()]));
        if (!result) {
            System.err.println("ERROR: Executing futures.");
        }
        futures.clear();
    }
}
