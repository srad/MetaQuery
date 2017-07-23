package com.github.srad.textimager;

import com.github.srad.textimager.reader.*;
import com.github.srad.textimager.storage.redis.StorageConsumer;
import com.github.srad.textimager.storage.type.AbstractStorageCommand;
import com.github.srad.textimager.storage.type.PoisonPillCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * The application executes three multi-threaded operations which use the producer-consumer pattern.
 * <ol>
 * <li>A parserQueue which enqueues the file parsing object with an xml event reader.</li>
 * <li>A storageQueue which takes there parsed elements and persists them.</li>
 * <li>The {@link ParserConsumer} mediates between these queue by starting the parsing process by
 * taking elements from the parsing queue, then taking the parsed elements and inserting {@link AbstractStorageCommand}
 * into the storageQueue which then consumes these object and sends it to a database.</li>
 * </ol>
 */
public class CasImporter {

    public CasImporter(CasImporterConfig config) throws IOException, InterruptedException {
        final ArrayBlockingQueue<AbstractParser> parserQueue = new ArrayBlockingQueue<>(500);
        final ArrayBlockingQueue<AbstractStorageCommand> storageQueue = new ArrayBlockingQueue<>(500);

        final ExecutorService executor = Executors.newCachedThreadPool();

        // You might want to reduce the {@link CasImporterConfig} threadCount property,
        // since we start two threads per available hardware (hyper)thread
        for (int i = 0; i < config.threadCount; i += 1) {
            executor.execute(new StorageConsumer(storageQueue));
            executor.execute(new ParserConsumer(parserQueue, storageQueue));
        }

        Thread parserThread = new Thread(new ParserProducer(new ParserConfig(parserQueue, config), file -> true));
        long start = System.currentTimeMillis();

        parserThread.start();
        parserThread.join();

        for (int i = 0; i < config.threadCount; i += 1) {
            storageQueue.put(new PoisonPillCommand());
            parserQueue.put(new ParserPoisonPill());
        }

        executor.shutdown();
        executor.awaitTermination(10L, TimeUnit.MINUTES);

        long end = System.currentTimeMillis();

        // log results
        String log = String.format("%s: Thread-count: %s, File-count: %s, Elapsed time: %ss\n", LocalDateTime.now(), config.threadCount, config.documentLimit, (end - start) / 1000);

        Path path = Paths.get("./thread.log");
        Files.write(path, log.getBytes(), (Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE));

        System.out.println(log);
    }
}
