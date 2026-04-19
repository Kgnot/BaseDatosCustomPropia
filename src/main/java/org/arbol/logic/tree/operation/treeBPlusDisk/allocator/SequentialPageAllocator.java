package org.arbol.logic.tree.operation.treeBPlusDisk.allocator;

public class SequentialPageAllocator implements PageAllocator {

    private long nextFree = 1L;

    @Override
    public long allocate() {
        return nextFree++;
    }

    @Override
    public void initialize(long nextFree) {
        this.nextFree = Math.max(1L, nextFree);
    }
}

