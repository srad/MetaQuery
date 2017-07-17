package main.java.org.srad.textimager.app;

import main.java.org.srad.textimager.reader.AbstractParser;
import main.java.org.srad.textimager.reader.CasDocumentParser;
import main.java.org.srad.textimager.reader.type.*;
import main.java.org.srad.textimager.storage.AbstractElementStore;
import main.java.org.srad.textimager.storage.Key;
import main.java.org.srad.textimager.storage.MapStorage;
import main.java.org.srad.textimager.storage.SortedSetStorage;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ParserConsumer implements Runnable {

    final private BlockingQueue<AbstractParser> queue;
    final private BlockingQueue<AbstractElementStore> storageQueue;

    final private HashMap<String, Long> freqLocal = new HashMap<>();

    public ParserConsumer(final BlockingQueue<AbstractParser> queue, final ArrayBlockingQueue<AbstractElementStore> counterQueue) {
        this.queue = queue;
        this.storageQueue = counterQueue;
    }

    public void run() {
        while (true) {
            // Parse page as a whole or not at all
            try {
                AbstractParser parser = queue.take();

                if (parser.isPoisonPill()) {
                    break;
                }

                parser.parse();

                // Count ElementTypes (grouped)
                for(Class<? extends ElementType> classType: CasDocumentParser.getParsedTypes()) {
                    final Stream<ElementType> elements = parser.filterType(classType);
                    final String typeName = classType.getSimpleName();

                    // group by token, char, lemma, ... and count
                    Map<String, Long> groupedByTextWithCount = elements.collect(Collectors.groupingBy(ElementType::getNormalizedText, Collectors.counting()));
                    final String keyDocCount = Key.create("doc", parser.getDocumentId(), "count", typeName);

                    storageQueue.put(new SortedSetStorage(keyDocCount, groupedByTextWithCount));
                }

                // Each element data storage (token, paragraph, sentence, lemma, ... except char, we do not store
                // single character with an id. Characters are just for type system regularity within the elements list.
                parser.getElements()
                        .parallelStream()
                        .filter(e -> (!e.getClass().equals(Char.class)))
                        .sequential()
                        .forEach(el -> {
                            try {
                                storageQueue.put(new MapStorage<>(Key.create("doc", parser.getDocumentId(), el.getTypeName(), el.id), el.toMap()));
                            } catch (Exception e) {
                                System.err.println(e.getMessage());
                            }
                        });

                // Store document data;
                storageQueue.put(new MapStorage<>(Key.create("doc", parser.getDocumentId(), "meta"), parser.getDocumentMeta()));
            } catch (Exception e) {
                System.err.printf("ERROR: %s\n", e.getMessage());
            }
        }
    }
}
