package org.srad.textimager.reader;

import com.sun.xml.internal.ws.streaming.XMLStreamReaderException;

import java.io.File;

public class ParserProducer implements Runnable {
    final private ParserConfig config;

    public ParserProducer(ParserConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        File[] files = new File(config.importFolder).listFiles(file -> file.isFile() && file.getName().endsWith("." + config.fileExtension));
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
