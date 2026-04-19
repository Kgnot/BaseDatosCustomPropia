package org.arbol.logic.tree.operation.treeBPlusDisk.allocator;

public interface PageAllocator {
    long allocate();
    void initialize(long nextFree);
}

