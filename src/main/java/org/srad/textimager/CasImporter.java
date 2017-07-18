package main.java.org.srad.textimager;

import com.lambdaworks.redis.LettuceFutures;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisHashAsyncCommands;
import main.java.org.srad.textimager.reader.*;
import main.java.org.srad.textimager.reader.type.ElementType;
import main.java.org.srad.textimager.storage.Key;
import main.java.org.srad.textimager.storage.redis.Config;
import main.java.org.srad.textimager.storage.type.AbstractStorageCommand;
import main.java.org.srad.textimager.storage.type.PoisonPillCommand;
import main.java.org.srad.textimager.storage.redis.StorageConsumer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The application executes three multi-threaded operations which use the producer-consumer pattern.
 * <ol>
 *   <li>A parserQueue which enqueues the file parsing object with an xml event reader.</li>
 *   <li>A storageQueue which takes there parsed elements and persists them.</li>
 *   <li>The {@link ParserConsumer} mediates between these queue by starting the parsing process by
 *       taking elements from the parsing queue, then taking the parsed elements and inserting {@link AbstractStorageCommand}
 *       into the storageQueue which then consumes these object and sends it to a database.</li>
 *   <li>All element frequencies are globally counted with the {@link #freqGlobal} {@link ConcurrentMap} and at the end
 *       written also to the database, but outside of the storageQueue, because the queue are consumed by multiple thread
 *       and this main-thread only knows when all threads are done and we can write the counters to the database.</li>
 * </ol>
 */
public class CasImporter {

    final private ConcurrentMap<String, Long> freqGlobal = new ConcurrentHashMap<>();

    public CasImporter(CasImporterConfig config) {
        try {

            final ArrayBlockingQueue<AbstractParser> parserQueue = new ArrayBlockingQueue<>(100);
            final ArrayBlockingQueue<AbstractStorageCommand> storageQueue = new ArrayBlockingQueue<>(200);

            final ExecutorService executor = Executors.newCachedThreadPool();

            // You might want to reduce the {@link CasImporterConfig} threadCount property,
            // since we start two threads per available hardware (hyper)thread
            for (int i = 0; i < config.threadCount; i += 1) {
                executor.execute(new StorageConsumer(storageQueue));
                executor.execute(new ParserConsumer(parserQueue, storageQueue, freqGlobal));
            }

            Thread parserThread = new Thread(new ParserProducer(new ParserConfig(parserQueue, config)));
            long start = System.currentTimeMillis();

            parserThread.start();
            parserThread.join();

            for (int i = 0; i < config.threadCount; i += 1) {
                parserQueue.put(new ParserPoisonPill());
                storageQueue.put(new PoisonPillCommand());
            }

            executor.shutdown();
            executor.awaitTermination(10L, TimeUnit.MINUTES);

            storeGlobalCounts();

            long end = System.currentTimeMillis();

            // log results
            String log = String.format("%s: Thread-count: %s, File-count: %s, Elapsed time: %ss\n", LocalDateTime.now(), config.threadCount, config.documentLimit, (end - start) / 1000);

            Path path = Paths.get("./thread.log");
            Files.write(path, log.getBytes(), (Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE));

            System.out.println(log);
        } catch (Exception e) {
            System.err.printf(e.getMessage());
        }
    }

    private void storeGlobalCounts() {
        final List<RedisFuture<?>> futures = new ArrayList<>();
        final RedisClient client = Config.createClient();
        final StatefulRedisConnection connection = client.connect();
        final HashMap<String, String> map = new HashMap<>();

        final RedisHashAsyncCommands<String, String> cmdHash = connection.async();

        freqGlobal.entrySet()
                .parallelStream()
                .collect(Collectors.groupingBy(s -> s.getKey().split(ElementType.TypeSeparator)[0]))
                .forEach((type, value) -> {
                    value.forEach(entry -> {
                        final Long count = entry.getValue();
                        final String text = entry.getKey().split(ElementType.TypeSeparator)[1];
                        final String key = Key.create("doc", "count", type);
                        // doc:count:<type> -> (<text>, <count>)
                        futures.add(cmdHash.hset(key, text,  String.valueOf(count)));

                        if (futures.size() > 400) {
                            flushFutures(connection, futures);
                        }
                    });
                });

        flushFutures(connection, futures);
    }

    private void flushFutures(StatefulRedisConnection connection, List<RedisFuture<?>> futures) {
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
