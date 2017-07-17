package main.java.org.srad.textimager.app;

import main.java.org.srad.textimager.reader.AbstractParser;
import main.java.org.srad.textimager.reader.ParserConfig;
import main.java.org.srad.textimager.reader.ParserProducer;
import main.java.org.srad.textimager.reader.ParserPoisonPill;
import main.java.org.srad.textimager.storage.*;
import main.java.org.srad.textimager.storage.AbstractElementStore;
import main.java.org.srad.textimager.storage.StoragePoisonPill;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.*;

public class App {

    public App(AppConfig config) {
        try {
            final ArrayBlockingQueue<AbstractParser> parserQueue = new ArrayBlockingQueue<>(100);
            final ArrayBlockingQueue<AbstractElementStore> storageQueue = new ArrayBlockingQueue<>(200);

            final ExecutorService executor = Executors.newCachedThreadPool();

            for (int i = 0; i < config.threadCount; i += 1) {
                executor.execute(new StorageConsumer(storageQueue));
                executor.execute(new ParserConsumer(parserQueue, storageQueue));
            }

            Thread parserThread = new Thread(new ParserProducer(new ParserConfig(parserQueue, config)));
            long start = System.currentTimeMillis();

            parserThread.start();
            parserThread.join();

            for (int i = 0; i < config.threadCount; i += 1) {
                parserQueue.put(new ParserPoisonPill());
                storageQueue.put(new StoragePoisonPill());
            }

            executor.shutdown();
            executor.awaitTermination(10L, TimeUnit.MINUTES);

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
}
