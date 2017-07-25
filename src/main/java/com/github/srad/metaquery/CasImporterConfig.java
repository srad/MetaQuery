package com.github.srad.metaquery;

import java.util.function.Consumer;

final public class CasImporterConfig {
    final public String importFolder;
    final public int threadCount;
    final public int documentLimit;
    final public String fileExtension;

    final public static int RedisPort = 6379;
    final public static int ArdbPort = 16379;
    final public static int WebServerPort = 8080;

    final public Consumer<String> statusCallback;

    public CasImporterConfig(final String importFolder) {
        this(importFolder, 0);
    }

    public CasImporterConfig(final String importFolder, final int documentLimit) {
        this(importFolder, documentLimit, Runtime.getRuntime().availableProcessors(), "xmi", s -> {});

    }

    public CasImporterConfig(final String importFolder, final int documentLimit, final int threadCount) {
        this(importFolder, documentLimit, threadCount, "xmi", s -> {});
    }

    public CasImporterConfig(final String importFolder, final int documentLimit, final int threadCount, final String fileExtension, final Consumer<String> statusCallback) {
        this.importFolder = importFolder;
        this.threadCount = threadCount;
        this.documentLimit = documentLimit;
        this.fileExtension = fileExtension;
        this.statusCallback = statusCallback;
    }
}
