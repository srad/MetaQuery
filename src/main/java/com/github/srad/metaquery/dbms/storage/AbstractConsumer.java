package com.github.srad.metaquery.dbms.storage;

import com.github.srad.metaquery.dbms.storage.type.AbstractStorageCommand;

import java.util.concurrent.ArrayBlockingQueue;

abstract public class AbstractConsumer implements Runnable {
    final protected ArrayBlockingQueue<AbstractStorageCommand> queue;

    protected AbstractConsumer(ArrayBlockingQueue<AbstractStorageCommand> queue) {
        this.queue = queue;
    }
}
