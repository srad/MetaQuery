package com.github.srad.metaquery.reader;

import com.github.srad.metaquery.MetaQueryConfig;

import java.util.concurrent.BlockingQueue;

final public class ParserConfig {
    final public BlockingQueue<AbstractParser> queue;
    final public String importFolder;
    final public String fileExtension;
    final public int fileLimit;

    public ParserConfig(final BlockingQueue<AbstractParser> queue, final MetaQueryConfig config) {
        this.queue = queue;
        this.importFolder = config.importFolder;
        this.fileExtension = config.fileExtension;
        this.fileLimit = config.documentLimit;
    }
}
