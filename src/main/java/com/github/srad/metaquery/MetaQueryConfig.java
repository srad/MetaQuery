package com.github.srad.metaquery;

final public class MetaQueryConfig {
    final public String importFolder;
    final public int threadCount;
    final public int documentLimit;
    final public String fileExtension;

    final public static int RedisPort = 6379;
    final public static int ArdbPort = 16379;
    final public static int WebServerPort = 8080;

    public MetaQueryConfig(final String importFolder) {
        this(importFolder, 0);
    }

    public MetaQueryConfig(final String importFolder, final int documentLimit) {
        this(importFolder, documentLimit, Runtime.getRuntime().availableProcessors(), "xmi");

    }

    public MetaQueryConfig(final String importFolder, final int documentLimit, final int threadCount) {
        this(importFolder, documentLimit, threadCount, "xmi");
    }

    public MetaQueryConfig(final String importFolder, final int documentLimit, final int threadCount, final String fileExtension) {
        this.importFolder = importFolder;
        this.threadCount = threadCount;
        this.documentLimit = documentLimit;
        this.fileExtension = fileExtension;
    }
}