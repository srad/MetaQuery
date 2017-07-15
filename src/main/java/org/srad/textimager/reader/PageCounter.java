package main.java.org.srad.textimager.reader;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PageCounter {
    ArrayBlockingQueue<AbstractParser> queue = new ArrayBlockingQueue<AbstractParser>(100);
    ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
    ExecutorService executor = Executors.newCachedThreadPool();
    /*

    // Half, ignore hyper threading
    int processors = Runtime.getRuntime().availableProcessors() / 2;

    for (int i = 0; i < processors; i += 1) {
        executor.execute(new Counter(queue, counts));
    }

    Thread parser = new Thread(new AbstractParser(queue));
    long start = System.currentTimeMillis();

    parser.start();
    parser.join();

    for (int i = 0; i < couters; i += 1) {
        queue.put(new PoisonPill());
    }

    executor.shutdown();
    executor.awaitTermination(10L, TimeUnit.MINUTES);

    long end = System.currentTimeMillis();
    System.out.println("Elapsed time: " + (end - start) + "ms");
    */
}
