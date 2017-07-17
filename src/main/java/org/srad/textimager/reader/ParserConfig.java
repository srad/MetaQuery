package main.java.org.srad.textimager.reader;

import main.java.org.srad.textimager.app.AppConfig;

import java.util.concurrent.BlockingQueue;

public class ParserConfig {
    final public BlockingQueue<AbstractParser> queue;
    final public String importFolder;
    final public String fileExtension;
    final public int fileLimit;

    public ParserConfig(final BlockingQueue<AbstractParser> queue, final AppConfig config) {
        this.queue = queue;
        this.importFolder = config.importFolder;
        this.fileExtension = config.fileExtension;
        this.fileLimit = config.documentLimit;
    }
}
