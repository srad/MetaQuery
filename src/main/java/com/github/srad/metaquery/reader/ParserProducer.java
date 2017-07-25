package com.github.srad.metaquery.reader;

import com.sun.xml.internal.ws.streaming.XMLStreamReaderException;

import java.io.File;
import java.util.function.Predicate;

public class ParserProducer implements Runnable {
    final private ParserConfig config;
    final private Predicate<File> fileFilter;

    public ParserProducer(ParserConfig config, Predicate<File> fileFilter) {
        this.config = config;
        this.fileFilter = fileFilter;
    }

    @Override
    public void run() {
        File[] files = new File(config.importFolder).listFiles(file -> file.isFile() && file.getName().endsWith("." + config.fileExtension) && fileFilter.test(file));
        int limit = ((config.fileLimit == 0) ? files.length : config.fileLimit);

        for (int i = 0; i < limit; i += 1) {
            try {
                config.queue.put(new CasDocumentParser(files[i]));
                if (i % 1000 == 0) {
                    System.out.printf("Parsed: %s/%s\n", i + 1, files.length);
                }
            } catch (XMLStreamReaderException e2) {
                // Ignore xmi parse exceptions
            } catch (Exception e3) {
                System.err.printf("ERROR: file(%s): %s\n", files[i].getAbsoluteFile(), e3.getMessage());
            }
        }
    }
}
