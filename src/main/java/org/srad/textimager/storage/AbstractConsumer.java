package org.srad.textimager.storage;

import org.srad.textimager.storage.type.AbstractStorageCommand;

import java.util.concurrent.ArrayBlockingQueue;

abstract public class AbstractConsumer implements Runnable {
    final protected ArrayBlockingQueue<AbstractStorageCommand> queue;

    protected AbstractConsumer(ArrayBlockingQueue<AbstractStorageCommand> queue) {
        this.queue = queue;
    }
}
