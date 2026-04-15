package org.arbol.logic.error;

public sealed interface NodeError {

    record NodeNotFoundError(Object key) implements NodeError {
    }

    record DuplicateKeyError(Object key) implements NodeError {
    }

    default String getMessage() {
        return switch (this) {
            case NodeNotFoundError(Object key) -> "Node not found with key: " + key.toString();
            case DuplicateKeyError(Object key) -> "Duplicate key error " + key.toString();
        };
    }
}
