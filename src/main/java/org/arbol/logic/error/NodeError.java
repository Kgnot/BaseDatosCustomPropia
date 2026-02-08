package org.arbol.logic.error;

import org.arbol.logic.nodes.Node;

public sealed interface NodeError {

    record NodeNotFoundError(Node<?, ?> node) implements NodeError {
    }

    record DuplicateKeyError(Object key) implements NodeError {
    }

    default String getMessage() {
        return switch (this) {
            case NodeNotFoundError(Node<?, ?> node) -> "Node not found " + node.toString();
            case DuplicateKeyError(Object key) -> "Duplicate key error " + key.toString();
        };
    }
}
