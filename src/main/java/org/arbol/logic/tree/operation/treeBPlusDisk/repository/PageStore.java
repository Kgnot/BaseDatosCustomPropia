package org.arbol.logic.tree.operation.treeBPlusDisk.repository;

public interface PageStore {
    byte[] read(long pageId);
    void write(long pageId, byte[] data);
    long totalPages();
    void close();
}

