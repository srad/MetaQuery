package com.github.srad.textimager.reader;

import com.github.srad.textimager.CasImporterConfig;

import java.util.concurrent.BlockingQueue;

public class ParserConfig {
    final public BlockingQueue<AbstractParser> queue;
    final public String importFolder;
    final public String fileExtension;
    final public int fileLimit;

    public ParserConfig(final BlockingQueue<AbstractParser> queue, final CasImporterConfig config) {
        this.queue = queue;
        this.importFolder = config.importFolder;
        this.fileExtension = config.fileExtension;
        this.fileLimit = config.documentLimit;
    }
}
