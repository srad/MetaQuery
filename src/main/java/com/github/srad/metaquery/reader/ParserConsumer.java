package com.github.srad.metaquery.reader;

import com.github.srad.metaquery.reader.type.Char;
import com.github.srad.metaquery.reader.type.ElementType;
import com.github.srad.metaquery.dbms.storage.type.*;
import com.github.srad.metaquery.dbms.storage.Key;

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

    public ParserConsumer(final BlockingQueue<AbstractParser> parserQueue, final ArrayBlockingQueue<AbstractStorageCommand> storageQueue) {
        this.queue = parserQueue;
        this.storageQueue = storageQueue;
    }

    public void run() {
        while (true) {
            // Parse page as a whole or not at all
            try {
                final AbstractParser parser = queue.take();

                if (parser.isPoisonPill()) {
                    break;
                }

                parser.parse();

                // Count ElementTypes (grouped)
                for(Class<? extends ElementType> classType: CasDocumentParser.getParsedTypes()) {
                    final Stream<ElementType> elements = parser.filterType(classType);
                    final String typeName = classType.getSimpleName();

                    // group by token, char, lemma, ... and count
                    final String keyDocCount = Key.create("doc", parser.getDocumentId(), "count", typeName);
                    final ConcurrentMap<String, Long> groupedByTextWithCount = parser.filterType(classType)
                            .collect(Collectors.groupingByConcurrent(ElementType::getText, Collectors.counting()));
                    storageQueue.put(new SortedSetCommand(keyDocCount, groupedByTextWithCount));

                    // Add all elements to a set
                    if (!classType.getSimpleName().equals(Char.class.getSimpleName())) {
                        final ConcurrentMap<String, List<ElementType>> groupedByTextWithElements = elements.collect(Collectors.groupingByConcurrent(ElementType::getText));
                        groupedByTextWithElements.forEach((string, list) -> {
                            list.forEach(elementInList -> {
                                final String setOfIdsOfType = Key.create("doc", parser.getDocumentId(), "set", typeName);
                                final String singleElementTypeData = Key.create("doc", parser.getDocumentId(), "element", typeName);
                                try {
                                    storageQueue.put(new SetAddCommand(setOfIdsOfType, elementInList.id));
                                    storageQueue.put(new MapCommand(singleElementTypeData, elementInList.toMap(elementInList.id + ":")));
                                } catch (InterruptedException e) {
                                    System.out.println(e.getMessage());
                                }
                            });
                        });
                    }
                }

                // Each element data storage (token, paragraph, sentence, lemma, ... except char, we do not store
                // single character with an id. Characters are just for type system regularity within the elements list.
                parser.getElements()
                        .parallelStream()
                        .filter(e -> (!e.getClass().equals(Char.class)))
                        .forEach(el -> {
                            try {
                                // union-set of all doc-ids which contain this element-type.
                                storageQueue.put(new SetAddCommand(Key.createUnionElementType(el.getTypeName(), el.getText()), parser.getDocumentId()));
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
