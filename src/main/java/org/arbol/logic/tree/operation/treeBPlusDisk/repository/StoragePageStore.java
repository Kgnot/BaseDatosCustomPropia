package org.arbol.logic.tree.operation.treeBPlusDisk.repository;

import org.arbol.logic.storage.StorageManager;

public class StoragePageStore implements PageStore {

    private final StorageManager storageManager;

    public StoragePageStore(String fileName) {
        this.storageManager = new StorageManager(fileName);
    }

    @Override
    public byte[] read(long pageId) {
        return storageManager.readPage(pageId);
    }

    @Override
    public void write(long pageId, byte[] data) {
        storageManager.writePage(pageId, data);
    }

    @Override
    public long totalPages() {
        return storageManager.getTotalPages();
    }

    @Override
    public void close() {
        storageManager.close();
    }
}

