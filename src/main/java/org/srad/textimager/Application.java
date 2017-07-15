package main.java.org.srad.textimager;

import com.sun.xml.internal.ws.streaming.XMLStreamReaderException;
import main.java.org.srad.textimager.net.Rest;
import main.java.org.srad.textimager.reader.CasParser;
import main.java.org.srad.textimager.reader.AbstractParser;
import main.java.org.srad.textimager.reader.type.ElementType;
import main.java.org.srad.textimager.reader.type.Token;
import main.java.org.srad.textimager.redis.Key;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.io.File;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Notice that the code here is written intentionally
 * procedural, static and final - for performance reasons.
 */
public class Application {

    private BlockingQueue<AbstractParser> queue;

    public Application(final String importFolder) {
        this(importFolder, "xmi");
    }

    public Application(final String importFolder, final String fileExtension) {
        //httpServer.start();

        if (importFolder != null) {
            //jedis.flushAll();

            File[] files = new File(importFolder).listFiles(file -> file.isFile() && file.getName().endsWith("." + fileExtension));

            for(int i=0; i < 1; i += 1) {
                try {
                    CasParser p = new CasParser(files[i]);
                    //p.filterType(Token.class).forEach(e -> System.out.println(e.toString()));

                    Stream<ElementType> elements = p.filterType(Token.class);
                    elements.collect(Collectors.groupingBy(ElementType::getNormalizedText));
                } catch (XMLStreamReaderException e2) {
                    // Ignore xmi parse exceptions
                } catch (Exception e3) {
                    System.err.printf("ERROR: '%s' -> %s\n", files[i].getAbsoluteFile(), e3.getMessage());
                }
            }

          //  p.sync();
          //  extractFeatures();
        }
    }

    final private static Jedis jedis = new Jedis("localhost", 6379, 60 * 60 * 24);

    static public Rest httpServer; // = new Rest();

    final static Pipeline p = jedis.pipelined();

    /**
     * Callbacks for Elements
     */
//    final private static HashMap<String, BiConsumer<String, HashMap<String, String>>> elementConsumers = new HashMap<String, BiConsumer<String, HashMap<String, String>>>() {{
//        put(CasParser.DocumentMetaData, (final String tagName, final HashMap<String, String> attributes) -> {
//            p.hmset(Key.DocTitle, Key.createMap(attributes.get("documentId"), attributes.get("documentTitle")));
//        });
//
//        put(CasParser.Sofa, (final String tagName, final HashMap<String, String> attributes) -> {
//            final String text = attributes.get("sofaString");
//            final String[] terms = Text.tokenize(Text.normalize(text));
//            final HashSet<String> termSet = new HashSet<>(Arrays.asList(terms));
//            final String DocTF = Key.create("doc", attributes.get("documentId"), "count", "tf");
//
//            // TF per document
//            for(String term: terms) {
//                p.zincrby(DocTF, 1, term);
//            }
//
//            // Global TF occurrence (NOT global frequency) or to rephrase: In how many documents does the term occur?
//            for(String token: termSet) {
//                p.zincrby(Key.TF_GLOBAL, 1, token);
//            }
//
//            p.hmset(Key.DocContent, Key.createMap(attributes.get("documentId"), text));
//        });
//
//        put(CasParser.Lemma, (final String tagName, final HashMap<String, String> attributes) -> {
//            // per doc-id lemma counter
//            p.zincrby(Key.create("doc", attributes.get("documentId"), "count", "lemma"), 1, attributes.get("value"));
//
//            // Global lemma counter
//            p.zincrby(Key.TotalLemmaCount, 1, attributes.get("value"));
//        });
//
//        // Since the :Sofa element is almost the last element BUT it contains the actual text, we must
//        // retain these following information and extract within an additional pass the text segments - once the import has ended.
//        put(CasParser.Paragraph, (final String tagName, final HashMap<String, String> attributes) -> p.hmset(Key.create("doc", attributes.get("documentId"), "paragraph", attributes.get("id")), attributes));
//        put(CasParser.Sentence, (final String tagName, final HashMap<String, String> attributes) -> p.hmset(Key.create("doc", attributes.get("documentId"), "sentence", attributes.get("id")), attributes));
//        put(CasParser.Token, (final String tagName, final HashMap<String, String> attributes) -> p.hmset(Key.create("doc", attributes.get("documentId"), "token", attributes.get("id")), attributes));
//    }};

    /**
     * Since the Sofa(+sofaString) Element almost occurs at last position, the XMLStreamReader
     * can extract for previous addedElements (like token) the Text segments, so:
     *
     * This is another pass through the database the extract these types from the sofaString
     * and writing them to the database for instant query.
     */
    private static void extractFeatures() {
        System.out.println("extract-start");

        scan(Key.DocContent, (final Map<String, String> map) -> {
            final int len = map.size();
            int i = 0;

            for (final String documentId : map.keySet()) {
                System.out.printf("%s/%s\n", String.valueOf(++i), String.valueOf(len));
                final String text = map.get(documentId);

                // Count characters
                for(int j=0; j < text.length(); j += 1) {
                    final String s = Character.toString(text.charAt(j));

                    if (s.equals(" ")) {
                        continue;
                    }

                    // Global
                    jedis.zincrby(Key.TotalChars, 1, s);
                    // Per document
                    jedis.zincrby(Key.create("doc", documentId, "count", "chars"), 1, s);
                }

                for (final String type : Arrays.asList("paragraph", "sentence", "token")) {
                    scan(Key.create("doc", documentId, type), (Map<String, String> element) -> {
                        try {
                            final String extracted = text.substring(Integer.valueOf(element.get("begin")), Integer.valueOf(element.get("end")));
                            jedis.zincrby(Key.create("count", "total", type), 1, extracted);
                            jedis.hset(Key.create("doc", documentId, "set", type), element.get("id"), extracted);
                            jedis.zincrby(Key.create("doc", documentId, "count", type), 1, extracted);
                        } catch (Exception e) {
                            System.err.printf("ERROR: doc-id: %s -> %s\n", documentId, e.getMessage());
                        }
                    });
                }
            }
        });

        System.out.println("extract-done");
    }

    private static void scan(String pattern, Consumer<Map<String, String>> consumer) {
        final ScanParams params = new ScanParams();
        params.match(pattern);

        // An iteration starts at "0": http://redis.io/commands/scan
        ScanResult<String> scanResult = jedis.scan("0", params);
        List<String> keys = scanResult.getResult();
        String nextCursor = scanResult.getStringCursor();

        while (true) {
            for (final String key : keys) {
                consumer.accept(jedis.hgetAll(key));
            }

            // An iteration also ends at "0"
            if (nextCursor.equals("0")) {
                break;
            }

            scanResult = jedis.scan(nextCursor, params);
            nextCursor = scanResult.getStringCursor();
            keys = scanResult.getResult();
        }
    }
}
