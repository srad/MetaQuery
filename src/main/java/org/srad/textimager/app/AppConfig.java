package main.java.org.srad.textimager.app;

import main.java.org.srad.textimager.main;

public class AppConfig {
    final public String importFolder;
    final public int threadCount;
    final public int documentLimit;
    final public String fileExtension;

    public AppConfig(final String importFolder) {
        this(importFolder, 0);
    }

    public AppConfig(final String importFolder, final int documentLimit) {
        this(importFolder, documentLimit, Runtime.getRuntime().availableProcessors(), "xmi");

    }

    public AppConfig(final String importFolder, final int documentLimit, final int threadCount) {
        this(importFolder, documentLimit, threadCount, "xmi");
    }

    public AppConfig(final String importFolder, final int documentLimit, final int threadCount, final String fileExtension) {
        this.importFolder = importFolder;
        this.threadCount = threadCount;
        this.documentLimit = documentLimit;
        this.fileExtension = fileExtension;
    }
}
