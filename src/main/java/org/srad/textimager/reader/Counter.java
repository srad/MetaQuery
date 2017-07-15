package main.java.org.srad.textimager.reader;

import main.java.org.srad.textimager.reader.type.ElementType;
import main.java.org.srad.textimager.reader.type.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Counter implements Runnable {

    private BlockingQueue<AbstractParser> queue;
    private ConcurrentMap<String, Integer> counts;
    private HashMap<String, Integer> localCounts;

    public Counter(BlockingQueue<AbstractParser> queue, ConcurrentMap<String, Integer> counts) {
        this.queue = queue;
        this.counts = counts;
        localCounts = new HashMap<>();
    }

    public void run() {
        try {
            while(true) {
                AbstractParser page = queue.take();

                if (page.isPoisonPill()) {
                    break;
                }

                Stream<ElementType> elements = page.filterType(Token.class);
                elements.collect(Collectors.groupingBy(ElementType::getName));

                //for (ElementType element: elements) {
                 //   localCounts.put(element.getText(), localCounts.putIfAbsent(element.getText(), 0) + 1);
                //}
            }
            mergeCounts();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mergeCounts() {
        for (Map.Entry<String, Integer> e: localCounts.entrySet()) {
            String word = e.getKey();
            Integer count = e.getValue();

            while (true) {
                Integer currentCount = counts.get(word);
                if (currentCount == null) {
                    if (counts.putIfAbsent(word, count) == null)
                        break;
                } else if (counts.replace(word, currentCount, currentCount + count)) {
                    break;
                }
            }
        }
    }
}