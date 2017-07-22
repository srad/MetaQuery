package com.github.srad.textimager.reader;

import com.github.srad.textimager.reader.type.Char;
import com.github.srad.textimager.reader.type.ElementType;
import com.github.srad.textimager.storage.type.*;
import com.github.srad.textimager.storage.Key;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mediation between the AbstractConsumer and AbstractParser.
 * Reads ElementType from the Parser and passes Storage command to the queue.
 * */
public class ParserConsumer implements Runnable {

    final private BlockingQueue<AbstractParser> queue;
    final private BlockingQueue<AbstractStorageCommand> storageQueue;

    final private ConcurrentMap<String, Long> freqGlobal;

    public ParserConsumer(final BlockingQueue<AbstractParser> parserQueue, final ArrayBlockingQueue<AbstractStorageCommand> storageQueue, final ConcurrentMap<String, Long> freqGlobal) {
        this.queue = parserQueue;
        this.storageQueue = storageQueue;
        this.freqGlobal = freqGlobal;
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
                    final Map<String, Long> groupedByTextWithCount = elements.collect(Collectors.groupingBy(ElementType::getNormalizedText, Collectors.counting()));
                    final String keyDocCount = Key.create("doc", parser.getDocumentId(), "count", typeName);

                    storageQueue.put(new SortedSetCommand(keyDocCount, groupedByTextWithCount));
                }

                /*
                // Global concurrent counter
                parser.getElements()
                        .stream()
                        .collect(Collectors.groupingByConcurrent(ElementType::getTextWithType, Collectors.counting()))
                        .forEach((key, count) -> {
                            freqGlobal.compute(key, (key2, globalCount) -> globalCount == null ? count : globalCount + count);
                        });
                        */

                // Each element data storage (token, paragraph, sentence, lemma, ... except char, we do not store
                // single character with an id. Characters are just for type system regularity within the elements list.
                parser.getElements()
                        .parallelStream()
                        .filter(e -> (!e.getClass().equals(Char.class)))
                        .sequential()
                        .forEach(el -> {
                            try {
                                storageQueue.put(new SetAddCommand(Key.createUnionElementType(el.getTypeName(), el.getNormalizedText()), parser.getDocumentId()));
                                //storageQueue.put(new MapCommand<>(Key.create("doc", parser.getDocumentId(), el.getTypeName(), el.id), el.toMap()));
                            } catch (Exception e) {
                                System.err.println(e.getMessage());
                            }
                        });

                // doc:<id>:meta -> id, title, text
                storageQueue.put(new DocumentCommand(Key.create("doc", parser.getDocumentId(), "meta"), parser.getDocumentMeta()));

                // doc:title -> id -> title
                storageQueue.put(new MapCommand<>(Key.create("doc", "title"), new HashMap<String, String>(){{ put(parser.getDocumentId(), parser.getDocumentTitle()); }}));
            } catch (Exception e) {
                System.err.printf("ERROR: %s\n", e.getMessage());
            }
        }
    }
}
